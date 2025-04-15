package group.quankane.util;

public enum SearchSortOperator {

    SEARCH_OPERATOR("(\\w+?)(:|<|>)(.*)"),
    SORT_BY("(\\w+?)(:)(.*)");

    private final String regex;

    SearchSortOperator(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }
}
