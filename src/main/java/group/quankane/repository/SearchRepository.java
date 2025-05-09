package group.quankane.repository;

import group.quankane.dto.response.PageResponse;
import group.quankane.model.Address;
import group.quankane.model.User;
import group.quankane.repository.criteria.SearchCriteria;
import group.quankane.repository.criteria.UserSearchQueryCriteriaConsumer;
import group.quankane.repository.specification.SpecSearchCriteria;
import group.quankane.util.AppConstants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static group.quankane.util.AppConstants.SORT_BY;
import static group.quankane.util.AppConstants.STR_FORMAT;

@Slf4j
@Repository
public class SearchRepository {
    @PersistenceContext
    private EntityManager entityManager;
    private static final String ADDRESS_ATTRIBUTE = "addresses";

    /**
     * Search user using keyword and
     *
     * @param pageNo
     * @param pageSize
     * @param search
     * @param sortBy
     * @return user list with sorting and paging
     */
    public PageResponse<List<User>> searchUser(int pageNo, int pageSize, String search, String sortBy) {
        log.info("Execute search user with keyword={}", search);

        StringBuilder sqlQuery = new StringBuilder("SELECT new vn.tayjava.dto.response.UserDetailResponse(u.id, u.firstName, u.lastName, u.phone, u.email) FROM User u WHERE 1=1");
        if (StringUtils.hasLength(search)) {
            sqlQuery.append(" AND lower(u.firstName) like lower(:firstName)");
            sqlQuery.append(" OR lower(u.lastName) like lower(:lastName)");
            sqlQuery.append(" OR lower(u.email) like lower(:email)");
        }

        if (StringUtils.hasLength(sortBy)) {
            // firstName:asc|desc
            Pattern pattern = Pattern.compile(SORT_BY);
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                sqlQuery.append(String.format(" ORDER BY u.%s %s", matcher.group(1), matcher.group(3)));
            }
        }

        // Get list of users
        Query selectQuery = entityManager.createQuery(sqlQuery.toString());
        if (StringUtils.hasLength(search)) {
            selectQuery.setParameter("firstName", String.format(STR_FORMAT, search));
            selectQuery.setParameter("lastName", String.format(STR_FORMAT, search));
            selectQuery.setParameter("email", String.format(STR_FORMAT, search));
        }
        selectQuery.setFirstResult(pageNo);
        selectQuery.setMaxResults(pageSize);
        List<User> users = selectQuery.getResultList();

        // Count users
        StringBuilder sqlCountQuery = new StringBuilder("SELECT COUNT(*) FROM User u");
        if (StringUtils.hasLength(search)) {
            sqlCountQuery.append(" WHERE lower(u.firstName) like lower(?1)");
            sqlCountQuery.append(" OR lower(u.lastName) like lower(?2)");
            sqlCountQuery.append(" OR lower(u.email) like lower(?3)");
        }

        Query countQuery = entityManager.createQuery(sqlCountQuery.toString());
        if (StringUtils.hasLength(search)) {
            countQuery.setParameter(1, String.format(STR_FORMAT, search));
            countQuery.setParameter(2, String.format(STR_FORMAT, search));
            countQuery.setParameter(3, String.format(STR_FORMAT, search));
            countQuery.getSingleResult();
        }

        Long totalElements = (Long) countQuery.getSingleResult();
        log.info("totalElements={}", totalElements);

        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<?> page = new PageImpl<>(users, pageable, totalElements);

        return PageResponse.<List<User>>builder()
                .page(pageNo)
                .size(pageSize)
                .total(page.getTotalPages())
                .items(users)
                .build();
    }

    /*
    * Advance search user by criteria
    *
    * @param offset
    * @param pageSize
    * @param sortBy
    * @param search
    * @return
    * */
    public PageResponse<List<User>> searchUserByCriteria (int offset, int pageSize, String sortBy, String address, String ... search) {
        log.info("Search user with search={} and sortBy={}", search, sortBy);

        List<SearchCriteria> criteriaList = new ArrayList<>();

        if (search.length > 0) {
            Pattern pattern = Pattern.compile(AppConstants.SEARCH_OPERATOR);
            for(String s : search) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    criteriaList.add (new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }

        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile(SORT_BY);
            for (String s : search) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    criteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }

        List<User> users = getUsers(offset, pageSize, criteriaList, address, sortBy);

        Long totalElements = getTotalElements(criteriaList);

        Page<User> page = new PageImpl<>(users, PageRequest.of(offset, pageSize), totalElements);

        return PageResponse.<List<User>>builder()
                .page(offset)
                .size(pageSize)
                .total(page.getTotalPages())
                .items(users)
                .build();
    }

    /**
     * Get all users with conditions
     *
     * @param offset
     * @param pageSize
     * @param criteriaList
     * @param sortBy
     * @return
     */
    private List<User> getUsers(int offset, int pageSize, List<SearchCriteria> criteriaList, String address, String sortBy) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = query.from(User.class);

        Predicate userPredicate = criteriaBuilder.conjunction();
        UserSearchQueryCriteriaConsumer searchConsumer = new UserSearchQueryCriteriaConsumer(userPredicate, criteriaBuilder, userRoot);

        criteriaList.forEach(searchConsumer);
        userPredicate = searchConsumer.getPredicate();

        if (StringUtils.hasLength(address)) {
            Join<Address, User> userAddressJoin = userRoot.join(ADDRESS_ATTRIBUTE);
            Predicate addressPredicate = criteriaBuilder.equal(userAddressJoin.get("city"), address);
            userPredicate = criteriaBuilder.and(userPredicate, addressPredicate);
        }
        query.where(userPredicate);

        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile(SORT_BY);
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                String fieldName = matcher.group(1);
                String direction = matcher.group(3);
                if (direction.equalsIgnoreCase("asc")) {
                    query.orderBy(criteriaBuilder.asc(userRoot.get(fieldName)));
                } else {
                    query.orderBy(criteriaBuilder.desc(userRoot.get(fieldName)));
                }
            }
        }
        return entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(pageSize)
                .getResultList();
    }

    /**
     * Count users with conditions
     *
     * @param params
     * @return
     */
    private Long getTotalElements(List<SearchCriteria> params) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<User> root = query.from(User.class);

        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchQueryCriteriaConsumer searchConsumer = new UserSearchQueryCriteriaConsumer(predicate, criteriaBuilder, root);
        params.forEach(searchConsumer);
        predicate = searchConsumer.getPredicate();
        query.select(criteriaBuilder.count(root));
        query.where(predicate);
        return entityManager.createQuery(query).getSingleResult();
    }

    /*
    * Search user join address
    *
    * @param pageable
    * @user
    * @address
    * @return
    * */

    public PageResponse<Object> searchUserByCriteriaWithJoin(Pageable pageable, String[] users, String[] addresses) {
        log.info("------------------searchUserByCriteriaWithJoin--------------------");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = criteriaQuery.from(User.class);
        Join<User, Address> addressRoot = userRoot.join(ADDRESS_ATTRIBUTE);

        List<Predicate> userPredicateList = new ArrayList<>();
        Pattern pattern = Pattern.compile(AppConstants.SEARCH_SPEC_OPERATOR);
        for (String s : users) {
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                SpecSearchCriteria specSearchCriteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                userPredicateList.add(toUserPredicate(userRoot, criteriaBuilder, specSearchCriteria));
            }
        }

        List<Predicate> addressPredicateList = new ArrayList<>();
        for (String s : addresses) {
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                SpecSearchCriteria specSearchCriteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                addressPredicateList.add(toAddressPredicate(addressRoot, criteriaBuilder, specSearchCriteria));
            }
        }

        Predicate userPredicate = criteriaBuilder.or(userPredicateList.toArray(new Predicate[0]));
        Predicate addressPredicate = criteriaBuilder.or(addressPredicateList.toArray(new Predicate[0]));
        Predicate finalPredicate = criteriaBuilder.and(userPredicate, addressPredicate);

        criteriaQuery.where(finalPredicate);

        List<User> userList = entityManager.createQuery(criteriaQuery)
                .setFirstResult(pageable.getPageNumber())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        long count = countUserJoinAddress(users, addresses);

        return PageResponse.builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .total(count)
                .items(userList)
                .build();
    }

    /*
    * Count user by conditions
    *
    * @param user
    * @param address
    * @return
    * */
    private Long countUserJoinAddress(String[] users, String[] addresses) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<User> userRoot = criteriaQuery.from(User.class);
        Join<User, Address> addressRoot = userRoot.join(ADDRESS_ATTRIBUTE);

        List<Predicate> userPredicateList = new ArrayList<>();
        Pattern pattern = Pattern.compile(AppConstants.SEARCH_SPEC_OPERATOR);

        for (String s : users) {
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                SpecSearchCriteria specSearchCriteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                userPredicateList.add(toUserPredicate(userRoot, criteriaBuilder, specSearchCriteria));
            }
        }

        List<Predicate> addressPredicateList = new ArrayList<>();
        for (String s : addresses) {
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                SpecSearchCriteria specSearchCriteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                addressPredicateList.add(toAddressPredicate(addressRoot, criteriaBuilder, specSearchCriteria));
            }
        }

        Predicate userPredicate = criteriaBuilder.or(userPredicateList.toArray(new Predicate[0]));
        Predicate addressPredicate = criteriaBuilder.or(addressPredicateList.toArray(new Predicate[0]));
        Predicate finalPredicate = criteriaBuilder.and(userPredicate, addressPredicate);

        criteriaQuery.select(criteriaBuilder.count(userRoot));
        criteriaQuery.where(finalPredicate);
        return entityManager.createQuery(criteriaQuery).getSingleResult();
    }

    private Predicate toUserPredicate (Root<User> userRoot, CriteriaBuilder criteriaBuilder, SpecSearchCriteria specSearchCriteria) {
        log.info("----------------------toUserPedicate---------------------");
        return switch (specSearchCriteria.getOperation()) {
            case EQUALITY ->
                    criteriaBuilder.equal(userRoot.get(specSearchCriteria.getKey()), specSearchCriteria.getValue());
            case NEGATION ->
                    criteriaBuilder.notEqual(userRoot.get(specSearchCriteria.getKey()), specSearchCriteria.getValue());
            case GREATER_THAN ->
                    criteriaBuilder.greaterThan(userRoot.get(specSearchCriteria.getKey()), specSearchCriteria.getValue().toString());
            case LESS_THAN ->
                    criteriaBuilder.lessThan(userRoot.get(specSearchCriteria.getKey()), specSearchCriteria.getValue().toString());
            case LIKE ->
                    criteriaBuilder.like(userRoot.get(specSearchCriteria.getKey()), "%" + specSearchCriteria.getValue() + "%");
            case STARTS_WITH ->
                    criteriaBuilder.equal(userRoot.get(specSearchCriteria.getKey()), specSearchCriteria.getValue() + "%");
            case ENDS_WITH ->
                    criteriaBuilder.equal(userRoot.get(specSearchCriteria.getKey()), "%" + specSearchCriteria.getValue());
            case CONTAINS ->
                    criteriaBuilder.equal(userRoot.get(specSearchCriteria.getKey()), "%" + specSearchCriteria.getValue() + "%");
        };
    }

    private Predicate toAddressPredicate (Join<User, Address> addressRoot, CriteriaBuilder criteriaBuilder, SpecSearchCriteria specSearchCriteria) {
        log.info("----------------------toAddressPedicate---------------------");
        return switch (specSearchCriteria.getOperation()) {
            case EQUALITY ->
                    criteriaBuilder.equal(addressRoot.get(specSearchCriteria.getKey()), specSearchCriteria.getValue());
            case NEGATION ->
                    criteriaBuilder.notEqual(addressRoot.get(specSearchCriteria.getKey()), specSearchCriteria.getValue());
            case GREATER_THAN ->
                    criteriaBuilder.greaterThan(addressRoot.get(specSearchCriteria.getKey()), specSearchCriteria.getValue().toString());
            case LESS_THAN ->
                    criteriaBuilder.lessThan(addressRoot.get(specSearchCriteria.getKey()), specSearchCriteria.getValue().toString());
            case LIKE ->
                    criteriaBuilder.like(addressRoot.get(specSearchCriteria.getKey()), "%" + specSearchCriteria.getValue() + "%");
            case STARTS_WITH ->
                    criteriaBuilder.equal(addressRoot.get(specSearchCriteria.getKey()), specSearchCriteria.getValue() + "%");
            case ENDS_WITH ->
                    criteriaBuilder.equal(addressRoot.get(specSearchCriteria.getKey()), "%" + specSearchCriteria.getValue());
            case CONTAINS ->
                    criteriaBuilder.equal(addressRoot.get(specSearchCriteria.getKey()), "%" + specSearchCriteria.getValue() + "%");
        };
    }

}
