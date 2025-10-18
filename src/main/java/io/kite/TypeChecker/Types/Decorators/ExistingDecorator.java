package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.ValueType;
import io.kite.Visitors.SyntaxPrinter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class ExistingDecorator extends DecoratorChecker {
    public static final String NAME = "existing";
    private final SyntaxPrinter printer;

    public ExistingDecorator(SyntaxPrinter syntaxPrinter) {
        super(NAME, decorator(
                        List.of(ValueType.String),
                        Set.of(DecoratorType.Target.RESOURCE)
                ), Set.of()
        );
        this.printer = syntaxPrinter;
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        var value = declaration.getValue();
        if (value == null) {
            throwIfInvalidArgs(declaration);
        }
        // maybe add these compile time checks so that we can catch them earlier?
//// S3 bucket ARN: arn:aws:s3:::bucket-name
//        private static final Pattern S3_BUCKET_ARN = Pattern.compile(
//                "^arn:(aws|aws-cn|aws-us-gov):s3:::(?<bucket>[a-z0-9][a-z0-9.-]{1,61}[a-z0-9])$"
//        );
//
//// S3 object ARN: arn:aws:s3:::bucket-name/key...
//        private static final Pattern S3_OBJECT_ARN = Pattern.compile(
//                "^arn:(aws|aws-cn|aws-us-gov):s3:::(?<bucket>[a-z0-9][a-z0-9.-]{1,61}[a-z0-9])/(?<key>.+)$"
//        );
//
//// IAM role: arn:partition:iam::ACCOUNT:role/PathOrName
//        private static final Pattern IAM_ROLE_ARN = Pattern.compile(
//                "^arn:(aws|aws-cn|aws-us-gov|aws-iso|aws-iso-b|aws-iso-e):iam::(?<account>\\d{12}|\\*):role\\/(?<name>[\\w+=,.@\\/-]{1,128})$"
//        );
//
//// Lambda function: arn:partition:lambda:region:account:function:functionName(:qualifier)?
//        private static final Pattern LAMBDA_FUNCTION_ARN = Pattern.compile(
//                "^arn:(aws|aws-cn|aws-us-gov|aws-iso|aws-iso-b|aws-iso-e):lambda:[a-z0-9-]+:(?<account>\\d{12}):function:(?<name>[a-zA-Z0-9-_]+)(?::[a-zA-Z0-9$_-]+)?$"
//        );
//
//// ECR repo: arn:partition:ecr:region:account:repository/repo-name
//        private static final Pattern ECR_REPO_ARN = Pattern.compile(
//                "^arn:(aws|aws-cn|aws-us-gov):ecr:[a-z0-9-]+:(?<account>\\d{12}):repository\\/(?<repo>[a-z0-9._/-]+)$"
//        );
//
//// CloudWatch log group: arn:partition:logs:region:account:log-group:group-name(:*)?
//        private static final Pattern LOG_GROUP_ARN = Pattern.compile(
//                "^arn:(aws|aws-cn|aws-us-gov):logs:[a-z0-9-]+:(?<account>\\d{12}):log-group:(?<group>[^:*]+)(?::.*)?$"
//        );
        switch (value) {
            case StringLiteral literal -> {
                if (StringUtils.isBlank(literal.getValue())) {
                    throwIfInvalidArgs(declaration);
                }
                var ref = ImportParsers.detect(literal.getValue());
                if (ref == null) {
                    throw new TypeError("%s has invalid argument: %s".formatted(printer.visit(declaration), printer.visit(value)));
                }

            }
            case String string -> {
                if (StringUtils.isBlank(string)) {
                    throwIfInvalidArgs(declaration);
                }
                var ref = ImportParsers.detect(string);
                if (ref == null) {
                    throw new TypeError("%s has invalid argument: %s".formatted(printer.visit(declaration), printer.visit(value)));
                }
            }
            default -> throwInvalidArgument(declaration, value);
        }

        return null;
    }

    private void throwInvalidArgument(AnnotationDeclaration declaration, Object value) {
        throw new TypeError("%s has invalid argument: %s".formatted(printer.visit(declaration), printer.visit(value)));
    }

    private void throwIfInvalidArgs(AnnotationDeclaration declaration) {
        throw new TypeError("%s must have a non-empty string as argument".formatted(printer.visit(declaration)));
    }

    public static final class ImportParsers {
        // Common patterns
        public static final Pattern ARN = Pattern.compile("^arn:[^:]+:[^:]*:[^:]*:[^:]*:.+$");
        public static final Pattern EC2_INSTANCE_ID = Pattern.compile("^i-[0-9a-f]{8,17}$");
        public static final Pattern S3_BUCKET = Pattern.compile("^[a-z0-9](?:[a-z0-9.-]{1,61})[a-z0-9]$");
        public static final Pattern KMS_ALIAS = Pattern.compile("^alias\\/[A-Za-z0-9/_+=.@-]{1,256}$");
        public static final Pattern ECR_REPO = Pattern.compile("^[a-z0-9._/-]+$");
        public static final Pattern LOG_GROUP = Pattern.compile("^\\/[^:*]+$");
        public static final Pattern URL = Pattern.compile("^https?://[^\\s]+$");
        public static final Pattern S3_URL = Pattern.compile("^(s3://)(?<bucket>[a-z0-9][a-z0-9.-]{1,61}[a-z0-9])(?<key>/.*)?$");
        public static final Pattern TAGS = Pattern.compile("^(?:[A-Za-z0-9._:/+=@-]+=[^,]+)(?:,(?:[A-Za-z0-9._:/+=@-]+=[^,]+))*$");

        public static boolean looksLikeArn(String s) { return s != null && ARN.matcher(s).matches(); }
        public static boolean looksLikeEc2InstanceId(String s) { return EC2_INSTANCE_ID.matcher(s).matches(); }
//        public static boolean looksLikeS3Bucket(String s) { return S3_BUCKET.matcher(s).matches(); }
        public static boolean looksLikeKmsAlias(String s) { return KMS_ALIAS.matcher(s).matches(); }
//        public static boolean looksLikeEcrRepo(String s) { return ECR_REPO.matcher(s).matches(); }
        public static boolean looksLikeLogGroup(String s) { return LOG_GROUP.matcher(s).matches(); }
        public static boolean looksLikeUrl(String s) { return URL.matcher(s).matches(); }
        public static boolean looksLikeS3Url(String s) { return S3_URL.matcher(s).matches(); }
        public static boolean looksLikeTagsClause(String s) { return TAGS.matcher(s).matches(); }

        /** Detect variant. Service/region/account may be filled later via context/flags. */
        public static ImportRef detect(String s) {
            if (StringUtils.isBlank(s)) return null;
            if (looksLikeArn(s)) return new ImportRef(ImportKind.ARN, null, s, null, null);
            if (looksLikeUrl(s) || looksLikeS3Url(s)) return new ImportRef(ImportKind.URL, null, s, null, null);
            if (looksLikeTagsClause(s)) return new ImportRef(ImportKind.TAGS, null, s, null, null);
            if (looksLikeEc2InstanceId(s)) return new ImportRef(ImportKind.SERVICE_ID, "ec2", s, null, null);
            if (looksLikeKmsAlias(s)) return new ImportRef(ImportKind.NAME, "kms", s, null, null);
            // fallbacks for common name forms:
//            if (looksLikeS3Bucket(s)) return new ImportRef(ImportKind.NAME, "s3", s, null, null);
            if (looksLikeLogGroup(s)) return new ImportRef(ImportKind.NAME, "logs", s, null, null);
//            if (looksLikeEcrRepo(s)) return new ImportRef(ImportKind.NAME, "ecr", s, null, null);
            // otherwise treat as generic NAME; require --service
            return new ImportRef(ImportKind.NAME, null, s, null, null);
        }
    }
    public enum ImportKind { ARN, SERVICE_ID, NAME, URL, TAGS }

    public record ImportRef(ImportKind kind, String service, String value,
                            String region, String account) {}
}
