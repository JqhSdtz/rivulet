package org.laputa.rivulet.module.data_model.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.jpa.AvailableHints;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.support.PageableUtils;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;

import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

/**
 * @author JQH
 * @since 下午 9:47 22/06/26
 */
@Service
public class DataModelService {

    @PersistenceContext
    private EntityManager entityManager;

    public Result<Page<RvPrototype>> queryDataModel(RvPrototype prototype, Pageable pageable) {
        Example<RvPrototype> example = Example.of(prototype);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<RvPrototype> query = builder.createQuery(RvPrototype.class);
        Root<RvPrototype> root = query.from(RvPrototype.class);
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
            Root<RvPrototype> countRoot = countQuery.from(RvPrototype.class);
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
        TypedQuery<RvPrototype> typedQuery = entityManager.createQuery(query);
        typedQuery.setHint(AvailableHints.HINT_CACHE_REGION, "defaultCache");
        typedQuery.setHint(AvailableHints.HINT_CACHEABLE, "true");
        typedQuery.setFirstResult(PageableUtils.getOffsetAsInteger(pageable));
        typedQuery.setMaxResults(pageable.getPageSize());
        List<RvPrototype> list = typedQuery.getResultList();
        Page<RvPrototype> pagedList = PageableExecutionUtils.getPage(list, pageable, countSupplier);
        return Result.succeed(pagedList);
    }
}
