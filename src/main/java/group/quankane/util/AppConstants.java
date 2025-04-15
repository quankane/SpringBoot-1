package group.quankane.util;

public class AppConstants {

    public static final String SEARCH_OPERATOR = "(\\w+?)(:|<|>)(.*)";
    public static final String SORT_BY = "(\\w+?)(:)(.*)";
    public static final String STR_FORMAT = "%%%s%%";

    // Private constructor to prevent instantiation
    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
