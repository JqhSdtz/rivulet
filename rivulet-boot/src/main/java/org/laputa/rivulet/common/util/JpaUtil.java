package org.laputa.rivulet.common.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.jpa.AvailableHints;
import org.laputa.rivulet.common.model.Pagination;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.dbms_model.entity.RvTable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.support.PageableUtils;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;

import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

@Component
public class JpaUtil {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 通用的分页查询工具方法，主要在js脚本中调用
     */
    public <T> Page<T> queryPage(T table, Class<T> tClass, Pagination pagination) {
        Example<T> example = Example.of(table);
        Pageable pageable = pagination.getPageable();
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(tClass);
        Root<T> root = query.from(tClass);
        Predicate predicate = QueryByExamplePredicateBuilder.getPredicate(root, builder, example, EscapeCharacter.DEFAULT);
        if (predicate != null) {
            query.where(predicate);
        }
        query.select(root);
        if (pageable.getSort().isSorted()) {
            query.orderBy(toOrders(pageable.getSort(), root, builder));
        }
        LongSupplier countSupplier = () -> {
            CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
            Root<T> countRoot = countQuery.from(tClass);
            if (countQuery.isDistinct()) {
                countQuery.select(builder.countDistinct(countRoot));
            } else {
                countQuery.select(builder.count(countRoot));
            }
            if (predicate != null) {
                countQuery.where(predicate);
            }
            countQuery.orderBy(Collections.emptyList());
            TypedQuery<Long> typedQuery = entityManager.createQuery(countQuery);
            typedQuery.setHint(AvailableHints.HINT_CACHE_REGION, "defaultCache");
            typedQuery.setHint(AvailableHints.HINT_CACHEABLE, "true");
            List<Long> totals = typedQuery.getResultList();
            long total = 0L;
            for (Long element : totals) {
                total += element == null ? 0 : element;
            }
            return total;
        };
        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        typedQuery.setHint(AvailableHints.HINT_CACHE_REGION, "defaultCache");
        typedQuery.setHint(AvailableHints.HINT_CACHEABLE, "true");
        typedQuery.setFirstResult(PageableUtils.getOffsetAsInteger(pageable));
        typedQuery.setMaxResults(pageable.getPageSize());
        List<T> list = typedQuery.getResultList();
        Page<T> pagedList = PageableExecutionUtils.getPage(list, pageable, countSupplier);
        return pagedList;
    }
}
