package io.zmeu.Frontend.Lexer;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

class TokenizerSpec {
    static List<Entry> spec = Arrays.asList(
// ---------------------------------- Comments --------------------------------------------------
            // Skip single line comment: //
            new Entry("^//.*[\\n]?", TokenType.Comment),
            // Skip multiline comment: /*  */
            new Entry("^\\/\\*[\\s\\S]*?\\*\\/[\\n]?", TokenType.Comment),
// --------------------------------- Grouping operators -----------------------------------------
            new Entry("^\\{", TokenType.OpenBraces),
            new Entry("^\\}", TokenType.CloseBraces),
            new Entry("^\\(", TokenType.OpenParenthesis),
            new Entry("^\\)", TokenType.CloseParenthesis),
            new Entry("^\\[", TokenType.OpenBrackets),
            new Entry("^\\]", TokenType.CloseBrackets),
            new Entry("^,", TokenType.Comma),
            new Entry("^\\.", TokenType.Dot),
// ---------------------------------- Literals ----------------------------------------------
            // match string: " str ", ' str '
            new Entry("^(\"|')[^(\"|')]*(\"|')", TokenType.String),
//            new Entry("^\\w+", TokenType.Type),
            // match numbers: 1, 111.222
            new Entry("^([0-9]*[.])?[0-9]+", TokenType.Number),

// --------------------------------- Whitespace --------------------------------------------------------
            new Entry("^\\n", TokenType.NewLine),
            new Entry("^\\s+", TokenType.WhiteSpace),
            new Entry("^;", TokenType.SemiColon),

// --------------------------------- Assignment --------------------------------------------------------
            new Entry("^:", TokenType.Colon),
            new Entry("^[!=]=", TokenType.Equality_Operator),
            // =, +=, -=, /=, *=
            new Entry("^=", TokenType.Equal),
            new Entry("^[\\+\\-\\*\\/]=", TokenType.Equal_Complex),

// --------------------------------- Operators --------------------------------------------------------
            new Entry("^\\+\\+", TokenType.Increment),
            new Entry("^\\+", TokenType.Plus),
            new Entry("^\\*", TokenType.Multiply),
            new Entry("^\\%", TokenType.Modulo),
            //todo add exponentiation ^
            new Entry("^--", TokenType.Decrement),
            new Entry("^(->)", TokenType.Lambda),
            new Entry("^-", TokenType.Minus),
// --------------------------------- Relational Operators: <,>, <=, >= --------------------------------------------------------
            new Entry("^[<>]=?", TokenType.RelationalOperator),
            // match / only if not followed by another /
            new Entry("^/(?!/)", TokenType.Division),
// --------------------------------- Logical Operators: <,>, <=, >= --------------------------------------------------------
            new Entry("^&&", TokenType.Logical_And),
            new Entry("^\\|\\|", TokenType.Logical_Or),
            new Entry("^!", TokenType.Logical_Not),

// --------------------------------- Keywords --------------------------------------------------------
//            new Entry("^\\blambda\\b", TokenType.Lambda),
            new Entry("^\\bvar\\b", TokenType.Var),
            new Entry("^\\bval\\b", TokenType.Val),
            new Entry("^\\bif\\b", TokenType.If),
            new Entry("^\\belse\\b", TokenType.Else),
            new Entry("^\\bwhile\\b", TokenType.While),
            new Entry("^\\bfor\\b", TokenType.For),
            new Entry("^\\btrue\\b", TokenType.True),
            new Entry("^\\bfalse\\b", TokenType.False),
            new Entry("^\\bnull\\b", TokenType.Null),
            new Entry("^\\bfun\\b", TokenType.Fun),
            new Entry("^\\breturn\\b", TokenType.Return),
            new Entry("^\\binit\\b", TokenType.Init),
            new Entry("^\\bthis\\b", TokenType.This),
            new Entry("^\\btype\\b", TokenType.Type),
            new Entry("^\\bexisting\\b", TokenType.Existing),
            new Entry("^\\bresource\\b", TokenType.Resource),
            new Entry("^\\bschema\\b", TokenType.Schema),
            new Entry("^\\bmodule\\b", TokenType.Module),
            new Entry("^\\bobject\\b", TokenType.Object),
            new Entry("^\\w+", TokenType.Identifier)
    );


    record Entry(Pattern pattern, TokenType type) {
        public Entry(Pattern pattern, TokenType type) {
            this.pattern = pattern;
            this.type = type;
        }

        public Entry(String pattern, TokenType type) {
            this(matcher(pattern), type);
        }

        private static Pattern matcher(String sequence) {
            return Pattern.compile(sequence.trim());
        }
    }
}


