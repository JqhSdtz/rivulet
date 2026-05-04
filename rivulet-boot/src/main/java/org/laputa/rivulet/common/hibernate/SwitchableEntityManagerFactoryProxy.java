package org.laputa.rivulet.common.hibernate;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.metamodel.Metamodel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SwitchableEntityManagerFactoryProxy implements InvocationHandler {

    private final AtomicReference<EntityManagerFactory> currentFactory;
    private final AtomicReference<EntityManagerFactory> oldFactoryToClose;
    private final AtomicInteger activeEntityManagers = new AtomicInteger(0);
    private final Object closeLock = new Object();

    public SwitchableEntityManagerFactoryProxy(EntityManagerFactory initial) {
        this.currentFactory = new AtomicReference<>(initial);
        this.oldFactoryToClose = new AtomicReference<>(null);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        EntityManagerFactory emf = currentFactory.get();

        if ("close".equals(method.getName()) && (args == null || args.length == 0)) {
            throw new UnsupportedOperationException("Cannot close proxy. Use swap() instead.");
        }

        // 拦截 createEntityManager：对所有创建出的 EntityManager 统一进行引用计数
        if ("createEntityManager".equals(method.getName())) {
            EntityManager rawEm;
            if (args == null || args.length == 0) {
                rawEm = emf.createEntityManager();
            } else if (args.length == 1 && args[0] instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) args[0];
                rawEm = emf.createEntityManager(map);
            } else {
                rawEm = (EntityManager) method.invoke(emf, args);
            }

            // 增加计数
            activeEntityManagers.incrementAndGet();
            // 用包装器接管 close
            return new CountingEntityManager(rawEm, activeEntityManagers,
                    oldFactoryToClose, closeLock);
        }

        return method.invoke(emf, args);
    }

    public void swap(EntityManagerFactory newFactory) {
        EntityManagerFactory oldFactory = currentFactory.getAndSet(newFactory);
        if (oldFactory != null) {
            // 告诉包装器哪个工厂需要被监控关闭
            oldFactoryToClose.set(oldFactory);
            // 立即检查：如果此刻活跃 EntityManager 已经为 0，直接关闭
            tryCloseOldFactory();
        }
    }

    // 每次有 EntityManager 关闭时都会调用这个方法
    private void tryCloseOldFactory() {
        if (activeEntityManagers.get() == 0) {
            synchronized (closeLock) {
                if (activeEntityManagers.get() == 0) {
                    EntityManagerFactory toClose = oldFactoryToClose.getAndSet(null);
                    if (toClose != null && toClose.isOpen()) {
                        toClose.close();
                    }
                }
            }
        }
    }

    // 包装 EntityManager 的静态内部类
    private static class CountingEntityManager implements EntityManager {
        private final EntityManager delegate;
        private final AtomicInteger counter;
        private final AtomicReference<EntityManagerFactory> oldFactoryRef;
        private final Object lock;

        CountingEntityManager(EntityManager delegate, AtomicInteger counter,
                              AtomicReference<EntityManagerFactory> oldFactoryRef, Object lock) {
            this.delegate = delegate;
            this.counter = counter;
            this.oldFactoryRef = oldFactoryRef;
            this.lock = lock;
        }

        /**
         * Close an application-managed entity manager.
         * After the close method has been invoked, all methods
         * on the <code>EntityManager</code> instance and any
         * <code>Query</code>, <code>TypedQuery</code>, and
         * <code>StoredProcedureQuery</code> objects obtained from
         * it will throw the <code>IllegalStateException</code>
         * except for <code>getProperties</code>,
         * <code>getTransaction</code>, and <code>isOpen</code> (which will return false).
         * If this method is called when the entity manager is
         * joined to an active transaction, the persistence
         * context remains managed until the transaction completes.
         *
         * @throws IllegalStateException if the entity manager
         *                               is container-managed
         */
        @Override
        public void close() {
            delegate.close();
            if (counter.decrementAndGet() == 0) {
                synchronized (lock) {
                    EntityManagerFactory toClose = oldFactoryRef.get();
                    if (toClose != null && counter.get() == 0) {
                        oldFactoryRef.set(null);
                        if (toClose.isOpen()) {
                            toClose.close();
                        }
                    }
                }
            }
        }

        /**
         * Make an instance managed and persistent.
         *
         * @param entity entity instance
         * @throws EntityExistsException        if the entity already exists.
         *                                      (If the entity already exists, the <code>EntityExistsException</code> may
         *                                      be thrown when the persist operation is invoked, or the
         *                                      <code>EntityExistsException</code> or another <code>PersistenceException</code> may be
         *                                      thrown at flush or commit time.)
         * @throws IllegalArgumentException     if the instance is not an
         *                                      entity
         * @throws TransactionRequiredException if there is no transaction when
         *                                      invoked on a container-managed entity manager of that is of type
         *                                      <code>PersistenceContextType.TRANSACTION</code>
         */
        @Override
        public void persist(Object entity) {
            delegate.persist(entity);
        }

        /**
         * Merge the state of the given entity into the
         * current persistence context.
         *
         * @param entity entity instance
         * @return the managed instance that the state was merged to
         * @throws IllegalArgumentException     if instance is not an
         *                                      entity or is a removed entity
         * @throws TransactionRequiredException if there is no transaction when
         *                                      invoked on a container-managed entity manager of that is of type
         *                                      <code>PersistenceContextType.TRANSACTION</code>
         */
        @Override
        public <T> T merge(T entity) {
            return delegate.merge(entity);
        }

        /**
         * Remove the entity instance.
         *
         * @param entity entity instance
         * @throws IllegalArgumentException     if the instance is not an
         *                                      entity or is a detached entity
         * @throws TransactionRequiredException if invoked on a
         *                                      container-managed entity manager of type
         *                                      <code>PersistenceContextType.TRANSACTION</code> and there is
         *                                      no transaction
         */
        @Override
        public void remove(Object entity) {
            delegate.remove(entity);
        }

        /**
         * Find by primary key.
         * Search for an entity of the specified class and primary key.
         * If the entity instance is contained in the persistence context,
         * it is returned from there.
         *
         * @param entityClass entity class
         * @param primaryKey  primary key
         * @return the found entity instance or null if the entity does
         * not exist
         * @throws IllegalArgumentException if the first argument does
         *                                  not denote an entity type or the second argument is
         *                                  is not a valid type for that entity's primary key or
         *                                  is null
         */
        @Override
        public <T> T find(Class<T> entityClass, Object primaryKey) {
            return delegate.find(entityClass, primaryKey);
        }

        /**
         * Find by primary key, using the specified properties.
         * Search for an entity of the specified class and primary key.
         * If the entity instance is contained in the persistence
         * context, it is returned from there.
         * If a vendor-specific property or hint is not recognized,
         * it is silently ignored.
         *
         * @param entityClass entity class
         * @param primaryKey  primary key
         * @param properties  standard and vendor-specific properties
         *                    and hints
         * @return the found entity instance or null if the entity does
         * not exist
         * @throws IllegalArgumentException if the first argument does
         *                                  not denote an entity type or the second argument is
         *                                  is not a valid type for that entity's primary key or
         *                                  is null
         * @since 2.0
         */
        @Override
        public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
            return delegate.find(entityClass, primaryKey, properties);
        }

        /**
         * Find by primary key and lock.
         * Search for an entity of the specified class and primary key
         * and lock it with respect to the specified lock type.
         * If the entity instance is contained in the persistence context,
         * it is returned from there, and the effect of this method is
         * the same as if the lock method had been called on the entity.
         * <p> If the entity is found within the persistence context and the
         * lock mode type is pessimistic and the entity has a version
         * attribute, the persistence provider must perform optimistic
         * version checks when obtaining the database lock.  If these
         * checks fail, the <code>OptimisticLockException</code> will be thrown.
         * <p>If the lock mode type is pessimistic and the entity instance
         * is found but cannot be locked:
         * <ul>
         * <li> the <code>PessimisticLockException</code> will be thrown if the database
         *    locking failure causes transaction-level rollback
         * <li> the <code>LockTimeoutException</code> will be thrown if the database
         *    locking failure causes only statement-level rollback
         * </ul>
         *
         * @param entityClass entity class
         * @param primaryKey  primary key
         * @param lockMode    lock mode
         * @return the found entity instance or null if the entity does
         * not exist
         * @throws IllegalArgumentException     if the first argument does
         *                                      not denote an entity type or the second argument is
         *                                      not a valid type for that entity's primary key or
         *                                      is null
         * @throws TransactionRequiredException if there is no
         *                                      transaction and a lock mode other than <code>NONE</code> is
         *                                      specified or if invoked on an entity manager which has
         *                                      not been joined to the current transaction and a lock
         *                                      mode other than <code>NONE</code> is specified
         * @throws OptimisticLockException      if the optimistic version
         *                                      check fails
         * @throws PessimisticLockException     if pessimistic locking
         *                                      fails and the transaction is rolled back
         * @throws LockTimeoutException         if pessimistic locking fails and
         *                                      only the statement is rolled back
         * @throws PersistenceException         if an unsupported lock call
         *                                      is made
         * @since 2.0
         */
        @Override
        public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
            return delegate.find(entityClass, primaryKey, lockMode);
        }

        /**
         * Find by primary key and lock, using the specified properties.
         * Search for an entity of the specified class and primary key
         * and lock it with respect to the specified lock type.
         * If the entity instance is contained in the persistence context,
         * it is returned from there.
         * <p> If the entity is found
         * within the persistence context and the lock mode type
         * is pessimistic and the entity has a version attribute, the
         * persistence provider must perform optimistic version checks
         * when obtaining the database lock.  If these checks fail,
         * the <code>OptimisticLockException</code> will be thrown.
         * <p>If the lock mode type is pessimistic and the entity instance
         * is found but cannot be locked:
         * <ul>
         * <li> the <code>PessimisticLockException</code> will be thrown if the database
         *    locking failure causes transaction-level rollback
         * <li> the <code>LockTimeoutException</code> will be thrown if the database
         *    locking failure causes only statement-level rollback
         * </ul>
         * <p>If a vendor-specific property or hint is not recognized,
         * it is silently ignored.
         * <p>Portable applications should not rely on the standard timeout
         * hint. Depending on the database in use and the locking
         * mechanisms used by the provider, the hint may or may not
         * be observed.
         *
         * @param entityClass entity class
         * @param primaryKey  primary key
         * @param lockMode    lock mode
         * @param properties  standard and vendor-specific properties
         *                    and hints
         * @return the found entity instance or null if the entity does
         * not exist
         * @throws IllegalArgumentException     if the first argument does
         *                                      not denote an entity type or the second argument is
         *                                      not a valid type for that entity's primary key or
         *                                      is null
         * @throws TransactionRequiredException if there is no
         *                                      transaction and a lock mode other than <code>NONE</code> is
         *                                      specified or if invoked on an entity manager which has
         *                                      not been joined to the current transaction and a lock
         *                                      mode other than <code>NONE</code> is specified
         * @throws OptimisticLockException      if the optimistic version
         *                                      check fails
         * @throws PessimisticLockException     if pessimistic locking
         *                                      fails and the transaction is rolled back
         * @throws LockTimeoutException         if pessimistic locking fails and
         *                                      only the statement is rolled back
         * @throws PersistenceException         if an unsupported lock call
         *                                      is made
         * @since 2.0
         */
        @Override
        public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
            return delegate.find(entityClass, primaryKey, lockMode, properties);
        }

        /**
         * Get an instance, whose state may be lazily fetched.
         * If the requested instance does not exist in the database,
         * the <code>EntityNotFoundException</code> is thrown when the instance
         * state is first accessed. (The persistence provider runtime is
         * permitted to throw the <code>EntityNotFoundException</code> when
         * <code>getReference</code> is called.)
         * The application should not expect that the instance state will
         * be available upon detachment, unless it was accessed by the
         * application while the entity manager was open.
         *
         * @param entityClass entity class
         * @param primaryKey  primary key
         * @return the found entity instance
         * @throws IllegalArgumentException if the first argument does
         *                                  not denote an entity type or the second argument is
         *                                  not a valid type for that entity's primary key or
         *                                  is null
         * @throws EntityNotFoundException  if the entity state
         *                                  cannot be accessed
         */
        @Override
        public <T> T getReference(Class<T> entityClass, Object primaryKey) {
            return delegate.getReference(entityClass, primaryKey);
        }

        /**
         * Synchronize the persistence context to the
         * underlying database.
         *
         * @throws TransactionRequiredException if there is
         *                                      no transaction or if the entity manager has not been
         *                                      joined to the current transaction
         * @throws PersistenceException         if the flush fails
         */
        @Override
        public void flush() {
            delegate.flush();
        }

        /**
         * Set the flush mode that applies to all objects contained
         * in the persistence context.
         *
         * @param flushMode flush mode
         */
        @Override
        public void setFlushMode(FlushModeType flushMode) {
            delegate.setFlushMode(flushMode);
        }

        /**
         * Get the flush mode that applies to all objects contained
         * in the persistence context.
         *
         * @return flushMode
         */
        @Override
        public FlushModeType getFlushMode() {
            return delegate.getFlushMode();
        }

        /**
         * Lock an entity instance that is contained in the persistence
         * context with the specified lock mode type.
         * <p>If a pessimistic lock mode type is specified and the entity
         * contains a version attribute, the persistence provider must
         * also perform optimistic version checks when obtaining the
         * database lock.  If these checks fail, the
         * <code>OptimisticLockException</code> will be thrown.
         * <p>If the lock mode type is pessimistic and the entity instance
         * is found but cannot be locked:
         * <ul>
         * <li> the <code>PessimisticLockException</code> will be thrown if the database
         *    locking failure causes transaction-level rollback
         * <li> the <code>LockTimeoutException</code> will be thrown if the database
         *    locking failure causes only statement-level rollback
         * </ul>
         *
         * @param entity   entity instance
         * @param lockMode lock mode
         * @throws IllegalArgumentException     if the instance is not an
         *                                      entity or is a detached entity
         * @throws TransactionRequiredException if there is no
         *                                      transaction or if invoked on an entity manager which
         *                                      has not been joined to the current transaction
         * @throws EntityNotFoundException      if the entity does not exist
         *                                      in the database when pessimistic locking is
         *                                      performed
         * @throws OptimisticLockException      if the optimistic version
         *                                      check fails
         * @throws PessimisticLockException     if pessimistic locking fails
         *                                      and the transaction is rolled back
         * @throws LockTimeoutException         if pessimistic locking fails and
         *                                      only the statement is rolled back
         * @throws PersistenceException         if an unsupported lock call
         *                                      is made
         */
        @Override
        public void lock(Object entity, LockModeType lockMode) {
            delegate.lock(entity, lockMode);
        }

        /**
         * Lock an entity instance that is contained in the persistence
         * context with the specified lock mode type and with specified
         * properties.
         * <p>If a pessimistic lock mode type is specified and the entity
         * contains a version attribute, the persistence provider must
         * also perform optimistic version checks when obtaining the
         * database lock.  If these checks fail, the
         * <code>OptimisticLockException</code> will be thrown.
         * <p>If the lock mode type is pessimistic and the entity instance
         * is found but cannot be locked:
         * <ul>
         * <li> the <code>PessimisticLockException</code> will be thrown if the database
         *    locking failure causes transaction-level rollback
         * <li> the <code>LockTimeoutException</code> will be thrown if the database
         *    locking failure causes only statement-level rollback
         * </ul>
         * <p>If a vendor-specific property or hint is not recognized,
         * it is silently ignored.
         * <p>Portable applications should not rely on the standard timeout
         * hint. Depending on the database in use and the locking
         * mechanisms used by the provider, the hint may or may not
         * be observed.
         *
         * @param entity     entity instance
         * @param lockMode   lock mode
         * @param properties standard and vendor-specific properties
         *                   and hints
         * @throws IllegalArgumentException     if the instance is not an
         *                                      entity or is a detached entity
         * @throws TransactionRequiredException if there is no
         *                                      transaction or if invoked on an entity manager which
         *                                      has not been joined to the current transaction
         * @throws EntityNotFoundException      if the entity does not exist
         *                                      in the database when pessimistic locking is
         *                                      performed
         * @throws OptimisticLockException      if the optimistic version
         *                                      check fails
         * @throws PessimisticLockException     if pessimistic locking fails
         *                                      and the transaction is rolled back
         * @throws LockTimeoutException         if pessimistic locking fails and
         *                                      only the statement is rolled back
         * @throws PersistenceException         if an unsupported lock call
         *                                      is made
         * @since 2.0
         */
        @Override
        public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
            delegate.lock(entity, lockMode, properties);
        }

        /**
         * Refresh the state of the instance from the database,
         * overwriting changes made to the entity, if any.
         *
         * @param entity entity instance
         * @throws IllegalArgumentException     if the instance is not
         *                                      an entity or the entity is not managed
         * @throws TransactionRequiredException if there is no
         *                                      transaction when invoked on a container-managed
         *                                      entity manager of type <code>PersistenceContextType.TRANSACTION</code>
         * @throws EntityNotFoundException      if the entity no longer
         *                                      exists in the database
         */
        @Override
        public void refresh(Object entity) {
            delegate.refresh(entity);
        }

        /**
         * Refresh the state of the instance from the database, using
         * the specified properties, and overwriting changes made to
         * the entity, if any.
         * <p> If a vendor-specific property or hint is not recognized,
         * it is silently ignored.
         *
         * @param entity     entity instance
         * @param properties standard and vendor-specific properties
         *                   and hints
         * @throws IllegalArgumentException     if the instance is not
         *                                      an entity or the entity is not managed
         * @throws TransactionRequiredException if there is no
         *                                      transaction when invoked on a container-managed
         *                                      entity manager of type <code>PersistenceContextType.TRANSACTION</code>
         * @throws EntityNotFoundException      if the entity no longer
         *                                      exists in the database
         * @since 2.0
         */
        @Override
        public void refresh(Object entity, Map<String, Object> properties) {
            delegate.refresh(entity, properties);
        }

        /**
         * Refresh the state of the instance from the database,
         * overwriting changes made to the entity, if any, and
         * lock it with respect to given lock mode type.
         * <p>If the lock mode type is pessimistic and the entity instance
         * is found but cannot be locked:
         * <ul>
         * <li> the <code>PessimisticLockException</code> will be thrown if the database
         *    locking failure causes transaction-level rollback
         * <li> the <code>LockTimeoutException</code> will be thrown if the
         *    database locking failure causes only statement-level
         *    rollback.
         * </ul>
         *
         * @param entity   entity instance
         * @param lockMode lock mode
         * @throws IllegalArgumentException     if the instance is not
         *                                      an entity or the entity is not managed
         * @throws TransactionRequiredException if invoked on a
         *                                      container-managed entity manager of type
         *                                      <code>PersistenceContextType.TRANSACTION</code> when there is
         *                                      no transaction; if invoked on an extended entity manager when
         *                                      there is no transaction and a lock mode other than <code>NONE</code>
         *                                      has been specified; or if invoked on an extended entity manager
         *                                      that has not been joined to the current transaction and a
         *                                      lock mode other than <code>NONE</code> has been specified
         * @throws EntityNotFoundException      if the entity no longer exists
         *                                      in the database
         * @throws PessimisticLockException     if pessimistic locking fails
         *                                      and the transaction is rolled back
         * @throws LockTimeoutException         if pessimistic locking fails and
         *                                      only the statement is rolled back
         * @throws PersistenceException         if an unsupported lock call
         *                                      is made
         * @since 2.0
         */
        @Override
        public void refresh(Object entity, LockModeType lockMode) {
            delegate.refresh(entity, lockMode);
        }

        /**
         * Refresh the state of the instance from the database,
         * overwriting changes made to the entity, if any, and
         * lock it with respect to given lock mode type and with
         * specified properties.
         * <p>If the lock mode type is pessimistic and the entity instance
         * is found but cannot be locked:
         * <ul>
         * <li> the <code>PessimisticLockException</code> will be thrown if the database
         *    locking failure causes transaction-level rollback
         * <li> the <code>LockTimeoutException</code> will be thrown if the database
         *    locking failure causes only statement-level rollback
         * </ul>
         * <p>If a vendor-specific property or hint is not recognized,
         *    it is silently ignored.
         * <p>Portable applications should not rely on the standard timeout
         * hint. Depending on the database in use and the locking
         * mechanisms used by the provider, the hint may or may not
         * be observed.
         *
         * @param entity     entity instance
         * @param lockMode   lock mode
         * @param properties standard and vendor-specific properties
         *                   and hints
         * @throws IllegalArgumentException     if the instance is not
         *                                      an entity or the entity is not managed
         * @throws TransactionRequiredException if invoked on a
         *                                      container-managed entity manager of type
         *                                      <code>PersistenceContextType.TRANSACTION</code> when there is
         *                                      no transaction; if invoked on an extended entity manager when
         *                                      there is no transaction and a lock mode other than <code>NONE</code>
         *                                      has been specified; or if invoked on an extended entity manager
         *                                      that has not been joined to the current transaction and a
         *                                      lock mode other than <code>NONE</code> has been specified
         * @throws EntityNotFoundException      if the entity no longer exists
         *                                      in the database
         * @throws PessimisticLockException     if pessimistic locking fails
         *                                      and the transaction is rolled back
         * @throws LockTimeoutException         if pessimistic locking fails and
         *                                      only the statement is rolled back
         * @throws PersistenceException         if an unsupported lock call
         *                                      is made
         * @since 2.0
         */
        @Override
        public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
            delegate.refresh(entity, lockMode, properties);
        }

        /**
         * Clear the persistence context, causing all managed
         * entities to become detached. Changes made to entities that
         * have not been flushed to the database will not be
         * persisted.
         */
        @Override
        public void clear() {
            delegate.clear();
        }

        /**
         * Remove the given entity from the persistence context, causing
         * a managed entity to become detached.  Unflushed changes made
         * to the entity if any (including removal of the entity),
         * will not be synchronized to the database.  Entities which
         * previously referenced the detached entity will continue to
         * reference it.
         *
         * @param entity entity instance
         * @throws IllegalArgumentException if the instance is not an
         *                                  entity
         * @since 2.0
         */
        @Override
        public void detach(Object entity) {
            delegate.detach(entity);
        }

        /**
         * Check if the instance is a managed entity instance belonging
         * to the current persistence context.
         *
         * @param entity entity instance
         * @return boolean indicating if entity is in persistence context
         * @throws IllegalArgumentException if not an entity
         */
        @Override
        public boolean contains(Object entity) {
            return delegate.contains(entity);
        }

        /**
         * Get the current lock mode for the entity instance.
         *
         * @param entity entity instance
         * @return lock mode
         * @throws TransactionRequiredException if there is no
         *                                      transaction or if the entity manager has not been
         *                                      joined to the current transaction
         * @throws IllegalArgumentException     if the instance is not a
         *                                      managed entity and a transaction is active
         * @since 2.0
         */
        @Override
        public LockModeType getLockMode(Object entity) {
            return delegate.getLockMode(entity);
        }

        /**
         * Set an entity manager property or hint.
         * If a vendor-specific property or hint is not recognized, it is
         * silently ignored.
         *
         * @param propertyName name of property or hint
         * @param value        value for property or hint
         * @throws IllegalArgumentException if the second argument is
         *                                  not valid for the implementation
         * @since 2.0
         */
        @Override
        public void setProperty(String propertyName, Object value) {
            delegate.setProperty(propertyName, value);
        }

        /**
         * Get the properties and hints and associated values that are in effect
         * for the entity manager. Changing the contents of the map does
         * not change the configuration in effect.
         *
         * @return map of properties and hints in effect for entity manager
         * @since 2.0
         */
        @Override
        public Map<String, Object> getProperties() {
            return delegate.getProperties();
        }

        /**
         * Create an instance of <code>Query</code> for executing a
         * Jakarta Persistence query language statement.
         *
         * @param qlString a Jakarta Persistence query string
         * @return the new query instance
         * @throws IllegalArgumentException if the query string is
         *                                  found to be invalid
         */
        @Override
        public Query createQuery(String qlString) {
            return delegate.createQuery(qlString);
        }

        /**
         * Create an instance of <code>TypedQuery</code> for executing a
         * criteria query.
         *
         * @param criteriaQuery a criteria query object
         * @return the new query instance
         * @throws IllegalArgumentException if the criteria query is
         *                                  found to be invalid
         * @since 2.0
         */
        @Override
        public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
            return delegate.createQuery(criteriaQuery);
        }

        /**
         * Create an instance of <code>Query</code> for executing a criteria
         * update query.
         *
         * @param updateQuery a criteria update query object
         * @return the new query instance
         * @throws IllegalArgumentException if the update query is
         *                                  found to be invalid
         * @since 2.1
         */
        @Override
        public Query createQuery(CriteriaUpdate updateQuery) {
            return delegate.createQuery(updateQuery);
        }

        /**
         * Create an instance of <code>Query</code> for executing a criteria
         * delete query.
         *
         * @param deleteQuery a criteria delete query object
         * @return the new query instance
         * @throws IllegalArgumentException if the delete query is
         *                                  found to be invalid
         * @since 2.1
         */
        @Override
        public Query createQuery(CriteriaDelete deleteQuery) {
            return delegate.createQuery(deleteQuery);
        }

        /**
         * Create an instance of <code>TypedQuery</code> for executing a
         * Jakarta Persistence query language statement.
         * The select list of the query must contain only a single
         * item, which must be assignable to the type specified by
         * the <code>resultClass</code> argument.
         *
         * @param qlString    a Jakarta Persistence query string
         * @param resultClass the type of the query result
         * @return the new query instance
         * @throws IllegalArgumentException if the query string is found
         *                                  to be invalid or if the query result is found to
         *                                  not be assignable to the specified type
         * @since 2.0
         */
        @Override
        public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
            return delegate.createQuery(qlString, resultClass);
        }

        /**
         * Create an instance of <code>Query</code> for executing a named query
         * (in the Jakarta Persistence query language or in native SQL).
         *
         * @param name the name of a query defined in metadata
         * @return the new query instance
         * @throws IllegalArgumentException if a query has not been
         *                                  defined with the given name or if the query string is
         *                                  found to be invalid
         */
        @Override
        public Query createNamedQuery(String name) {
            return delegate.createNamedQuery(name);
        }

        /**
         * Create an instance of <code>TypedQuery</code> for executing a
         * Jakarta Persistence query language named query.
         * The select list of the query must contain only a single
         * item, which must be assignable to the type specified by
         * the <code>resultClass</code> argument.
         *
         * @param name        the name of a query defined in metadata
         * @param resultClass the type of the query result
         * @return the new query instance
         * @throws IllegalArgumentException if a query has not been
         *                                  defined with the given name or if the query string is
         *                                  found to be invalid or if the query result is found to
         *                                  not be assignable to the specified type
         * @since 2.0
         */
        @Override
        public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
            return delegate.createNamedQuery(name, resultClass);
        }

        /**
         * Create an instance of <code>Query</code> for executing
         * a native SQL statement, e.g., for update or delete.
         * If the query is not an update or delete query, query
         * execution will result in each row of the SQL result
         * being returned as a result of type Object[] (or a result
         * of type Object if there is only one column in the select
         * list.)  Column values are returned in the order of their
         * appearance in the select list and default JDBC type
         * mappings are applied.
         *
         * @param sqlString a native SQL query string
         * @return the new query instance
         */
        @Override
        public Query createNativeQuery(String sqlString) {
            return delegate.createNativeQuery(sqlString);
        }

        /**
         * Create an instance of <code>Query</code> for executing
         * a native SQL query.
         *
         * @param sqlString   a native SQL query string
         * @param resultClass the class of the resulting instance(s)
         * @return the new query instance
         */
        @Override
        public Query createNativeQuery(String sqlString, Class resultClass) {
            return delegate.createNativeQuery(sqlString, resultClass);
        }

        /**
         * Create an instance of <code>Query</code> for executing
         * a native SQL query.
         *
         * @param sqlString        a native SQL query string
         * @param resultSetMapping the name of the result set mapping
         * @return the new query instance
         */
        @Override
        public Query createNativeQuery(String sqlString, String resultSetMapping) {
            return delegate.createNativeQuery(sqlString, resultSetMapping);
        }

        /**
         * Create an instance of <code>StoredProcedureQuery</code> for executing a
         * stored procedure in the database.
         * <p>Parameters must be registered before the stored procedure can
         * be executed.
         * <p>If the stored procedure returns one or more result sets,
         * any result set will be returned as a list of type Object[].
         *
         * @param name name assigned to the stored procedure query
         *             in metadata
         * @return the new stored procedure query instance
         * @throws IllegalArgumentException if a query has not been
         *                                  defined with the given name
         * @since 2.1
         */
        @Override
        public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
            return delegate.createNamedStoredProcedureQuery(name);
        }

        /**
         * Create an instance of <code>StoredProcedureQuery</code> for executing a
         * stored procedure in the database.
         * <p>Parameters must be registered before the stored procedure can
         * be executed.
         * <p>If the stored procedure returns one or more result sets,
         * any result set will be returned as a list of type Object[].
         *
         * @param procedureName name of the stored procedure in the
         *                      database
         * @return the new stored procedure query instance
         * @throws IllegalArgumentException if a stored procedure of the
         *                                  given name does not exist (or the query execution will
         *                                  fail)
         * @since 2.1
         */
        @Override
        public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
            return delegate.createStoredProcedureQuery(procedureName);
        }

        /**
         * Create an instance of <code>StoredProcedureQuery</code> for executing a
         * stored procedure in the database.
         * <p>Parameters must be registered before the stored procedure can
         * be executed.
         * <p>The <code>resultClass</code> arguments must be specified in the order in
         * which the result sets will be returned by the stored procedure
         * invocation.
         *
         * @param procedureName name of the stored procedure in the
         *                      database
         * @param resultClasses classes to which the result sets
         *                      produced by the stored procedure are to
         *                      be mapped
         * @return the new stored procedure query instance
         * @throws IllegalArgumentException if a stored procedure of the
         *                                  given name does not exist (or the query execution will
         *                                  fail)
         * @since 2.1
         */
        @Override
        public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
            return delegate.createStoredProcedureQuery(procedureName, resultClasses);
        }

        /**
         * Create an instance of <code>StoredProcedureQuery</code> for executing a
         * stored procedure in the database.
         * <p>Parameters must be registered before the stored procedure can
         * be executed.
         * <p>The <code>resultSetMapping</code> arguments must be specified in the order
         * in which the result sets will be returned by the stored
         * procedure invocation.
         *
         * @param procedureName     name of the stored procedure in the
         *                          database
         * @param resultSetMappings the names of the result set mappings
         *                          to be used in mapping result sets
         *                          returned by the stored procedure
         * @return the new stored procedure query instance
         * @throws IllegalArgumentException if a stored procedure or
         *                                  result set mapping of the given name does not exist
         *                                  (or the query execution will fail)
         */
        @Override
        public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
            return delegate.createStoredProcedureQuery(procedureName, resultSetMappings);
        }

        /**
         * Indicate to the entity manager that a JTA transaction is
         * active and join the persistence context to it.
         * <p>This method should be called on a JTA application
         * managed entity manager that was created outside the scope
         * of the active transaction or on an entity manager of type
         * <code>SynchronizationType.UNSYNCHRONIZED</code> to associate
         * it with the current JTA transaction.
         *
         * @throws TransactionRequiredException if there is
         *                                      no transaction
         */
        @Override
        public void joinTransaction() {
            delegate.joinTransaction();
        }

        /**
         * Determine whether the entity manager is joined to the
         * current transaction. Returns false if the entity manager
         * is not joined to the current transaction or if no
         * transaction is active
         *
         * @return boolean
         * @since 2.1
         */
        @Override
        public boolean isJoinedToTransaction() {
            return delegate.isJoinedToTransaction();
        }

        /**
         * Return an object of the specified type to allow access to the
         * provider-specific API.   If the provider's <code>EntityManager</code>
         * implementation does not support the specified class, the
         * <code>PersistenceException</code> is thrown.
         *
         * @param cls the class of the object to be returned.  This is
         *            normally either the underlying <code>EntityManager</code> implementation
         *            class or an interface that it implements.
         * @return an instance of the specified class
         * @throws PersistenceException if the provider does not
         *                              support the call
         * @since 2.0
         */
        @Override
        public <T> T unwrap(Class<T> cls) {
            return delegate.unwrap(cls);
        }

        /**
         * Return the underlying provider object for the <code>EntityManager</code>,
         * if available. The result of this method is implementation
         * specific.
         * <p>The <code>unwrap</code> method is to be preferred for new applications.
         *
         * @return underlying provider object for EntityManager
         */
        @Override
        public Object getDelegate() {
            return delegate.getDelegate();
        }

        /**
         * Determine whether the entity manager is open.
         *
         * @return true until the entity manager has been closed
         */
        @Override
        public boolean isOpen() {
            return delegate.isOpen();
        }

        /**
         * Return the resource-level <code>EntityTransaction</code> object.
         * The <code>EntityTransaction</code> instance may be used serially to
         * begin and commit multiple transactions.
         *
         * @return EntityTransaction instance
         * @throws IllegalStateException if invoked on a JTA
         *                               entity manager
         */
        @Override
        public EntityTransaction getTransaction() {
            return delegate.getTransaction();
        }

        /**
         * Return the entity manager factory for the entity manager.
         *
         * @return EntityManagerFactory instance
         * @throws IllegalStateException if the entity manager has
         *                               been closed
         * @since 2.0
         */
        @Override
        public EntityManagerFactory getEntityManagerFactory() {
            return delegate.getEntityManagerFactory();
        }

        /**
         * Return an instance of <code>CriteriaBuilder</code> for the creation of
         * <code>CriteriaQuery</code> objects.
         *
         * @return CriteriaBuilder instance
         * @throws IllegalStateException if the entity manager has
         *                               been closed
         * @since 2.0
         */
        @Override
        public CriteriaBuilder getCriteriaBuilder() {
            return delegate.getCriteriaBuilder();
        }

        /**
         * Return an instance of <code>Metamodel</code> interface for access to the
         * metamodel of the persistence unit.
         *
         * @return Metamodel instance
         * @throws IllegalStateException if the entity manager has
         *                               been closed
         * @since 2.0
         */
        @Override
        public Metamodel getMetamodel() {
            return delegate.getMetamodel();
        }

        /**
         * Return a mutable EntityGraph that can be used to dynamically create an
         * EntityGraph.
         *
         * @param rootType class of entity graph
         * @return entity graph
         * @since 2.1
         */
        @Override
        public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
            return delegate.createEntityGraph(rootType);
        }

        /**
         * Return a mutable copy of the named EntityGraph.  If there
         * is no entity graph with the specified name, null is returned.
         *
         * @param graphName name of an entity graph
         * @return entity graph
         * @since 2.1
         */
        @Override
        public EntityGraph<?> createEntityGraph(String graphName) {
            return delegate.createEntityGraph(graphName);
        }

        /**
         * Return a named EntityGraph. The returned EntityGraph
         * should be considered immutable.
         *
         * @param graphName name of an existing entity graph
         * @return named entity graph
         * @throws IllegalArgumentException if there is no EntityGraph of
         *                                  the given name
         * @since 2.1
         */
        @Override
        public EntityGraph<?> getEntityGraph(String graphName) {
            return delegate.getEntityGraph(graphName);
        }

        /**
         * Return all named EntityGraphs that have been defined for the provided
         * class type.
         *
         * @param entityClass entity class
         * @return list of all entity graphs defined for the entity
         * @throws IllegalArgumentException if the class is not an entity
         * @since 2.1
         */
        @Override
        public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
            return delegate.getEntityGraphs(entityClass);
        }
    }
}