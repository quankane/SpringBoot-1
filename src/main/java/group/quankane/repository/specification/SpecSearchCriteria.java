package group.quankane.repository.specification;

import lombok.Getter;

import static group.quankane.repository.specification.SearchOperation.*;

@Getter
public class SpecSearchCriteria {

    private String key;
    private SearchOperation operation;
    private Object value;
    private boolean orPredicate;

    public SpecSearchCriteria(final String key, final SearchOperation operation, final Object value) {
        super();
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public SpecSearchCriteria(final String orPredicate, final String key, final SearchOperation operation, final Object value) {
        super();
        this.orPredicate = orPredicate != null && orPredicate.equals(OR_PREDICATE_FLAG);
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public SpecSearchCriteria(String key, String operation, String value, String prefix, String suffix) {
        SearchOperation searchOperation = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (searchOperation == EQUALITY) {
            final boolean startWithAsterisk = prefix != null && prefix.contains(ZERO_OR_MORE_REGEX);
            final boolean endWidthAsterisk = suffix != null && suffix.contains(ZERO_OR_MORE_REGEX);

            if (startWithAsterisk && endWidthAsterisk) {
                searchOperation = CONTAINS;
            } else if (startWithAsterisk) {
                searchOperation = ENDS_WITH;
            } else if (endWidthAsterisk) {
                searchOperation = STARTS_WITH;
            }
        }
        this.key = key;
        this.operation = searchOperation;
        this.value = value;
    }
}
