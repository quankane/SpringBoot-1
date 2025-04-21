package group.quankane.repository.specification;

import group.quankane.model.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecificationBuilder {

    public final List<SpecSearchCriteria> params;

    public UserSpecificationBuilder() {
        this.params = new ArrayList<>();
    }

    //API
    public UserSpecificationBuilder with (final String key, final String operation, final Object value, final String prefix, final String suffic) {
        return with(null, key, operation, value, prefix, suffic);
    }

    public UserSpecificationBuilder with (final String orPredicate, final String key, final String operation, final Object value, final String prefix, final String suffic) {
        SearchOperation searchOperation = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (searchOperation != null) {
            if (searchOperation == SearchOperation.EQUALITY) {
                final boolean startWithAsterisk = prefix != null && prefix.contains(SearchOperation.ZERO_OR_MORE_REGEX);
                final boolean endWithAsterisk = suffic != null && suffic.contains(SearchOperation.ZERO_OR_MORE_REGEX);
                if (startWithAsterisk && endWithAsterisk) {
                    searchOperation = SearchOperation.CONTAINS;
                } else if (startWithAsterisk) {
                    searchOperation = SearchOperation.ENDS_WITH;
                } else if (endWithAsterisk) {
                    searchOperation = SearchOperation.STARTS_WITH;
                }
            }
            params.add(new SpecSearchCriteria(orPredicate, key, searchOperation, value));
        }
        return this;
    }

    public Specification<User> build () {
        if (params.isEmpty()) {
            return null;
        }

        Specification<User> result = new UserSpecification(params.get(0));

        for (int i = 1; i < params.size(); i++) {
            result = params.get(i).isOrPredicate() ?
                    Specification.where(result).or(new UserSpecification(params.get(i)))
                    : Specification.where(result).and(new UserSpecification(params.get(i)));
        }

        return result;
    }

    public UserSpecificationBuilder with (UserSpecification userSpecification) {
        params.add(userSpecification.getCriteria());
        return this;
    }

    public UserSpecificationBuilder with (SpecSearchCriteria specSearchCriteria) {
        params.add(specSearchCriteria);
        return this;
    }
}
