package io.zmeu.Frontend.Parser.errors;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class ErrorList extends RuntimeException{
    private List<ParseError> errors;
}
