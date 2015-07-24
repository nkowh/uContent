package com.nikoyo.odata.persistence;


import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmMember;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.*;
import org.apache.olingo.server.api.uri.queryoption.expression.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilterVisitor implements ExpressionVisitor {

    public Object visit(Expression expression) throws ODataApplicationException {
        try {
            if (expression instanceof Member) {
                Member member = (Member) expression;
                return visitMember(member.getResourcePath());
            } else if (expression instanceof Literal) {
                Literal literal = (Literal) expression;
                return visitLiteral(literal.getText());
            } else if (expression instanceof TypeLiteral) {

            } else if (expression instanceof Method) {
                Method method = (Method) expression;
                return visitMethodCall(method.getMethod(), method.getParameters());

            } else if (expression instanceof LambdaRef) {

            } else if (expression instanceof Alias) {

            } else if (expression instanceof Unary) {

            } else if (expression instanceof Enumeration) {
                Enumeration enumeration = (Enumeration) expression;
                return visitEnum(enumeration.getType(), enumeration.getValues());
            } else if (expression instanceof Binary) {
                Binary binary = (Binary) expression;
                return visitBinaryOperator(binary.getOperator(), visit(binary.getLeftOperand()), visit(binary.getRightOperand()));
            }

        } catch (Exception e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.EXPECTATION_FAILED.getStatusCode(), Locale.ENGLISH);
        }

        throw new ODataApplicationException("Unsupported expression:" + expression.toString(), HttpStatusCode.EXPECTATION_FAILED.getStatusCode(), Locale.ENGLISH);
    }


    @Override
    public Object visitBinaryOperator(BinaryOperatorKind operator, Object left, Object right) throws ExpressionVisitException, ODataApplicationException {
        String sqlOperator = "";
        switch (operator) {
            case EQ:
                sqlOperator = "=";
                break;
            case NE:
                sqlOperator = "<>";
                break;
            case OR:
                sqlOperator = "OR";
                break;
            case AND:
                sqlOperator = "AND";
                break;
            case GE:
                sqlOperator = ">=";
                break;
            case GT:
                sqlOperator = ">";
                break;
            case LE:
                sqlOperator = "<=";
                break;
            case LT:
                sqlOperator = "<";
                break;
            default:
                throw new ODataApplicationException("Unsupported operator:" + operator.toString(), HttpStatusCode.EXPECTATION_FAILED.getStatusCode(), Locale.ENGLISH);
        }
        //return the binary statement
        return left + " " + sqlOperator + " " + right;
    }

    @Override
    public Object visitUnaryOperator(UnaryOperatorKind operator, Object operand) throws ExpressionVisitException, ODataApplicationException {
        return null;
    }

    @Override
    public Object visitMethodCall(MethodKind methodCall, List parameters) throws ExpressionVisitException, ODataApplicationException {
        List args = new ArrayList();
        for (Object parameter : parameters) {
            args.add(visit((Expression) parameter));
        }
        switch (methodCall) {
            case CONTAINS:
                return "POSITION(" + args.get(1) + " in " + args.get(0) + ")>0";
            case STARTSWITH:
                return "POSITION(" + args.get(1) + " in " + args.get(0) + ")=0";
            case ENDSWITH:
                return "POSITION(" + args.get(1) + " in " + args.get(0) + ") = (char_length(" + args.get(0) + ")-char_length(" + args.get(1) + ")+1)";
            case LENGTH:
                return "char_length(" + args.get(0) + ")";
            case INDEXOF:
                return "(POSITION(" + args.get(1) + " in " + args.get(0) + ") -1)";
            case SUBSTRING:
                break;
            case TOLOWER:
                break;
            case TOUPPER:
                break;
            case TRIM:
                break;
            case CONCAT:
                break;
            case YEAR:
                return "date_part('year'," + args.get(0) + ")";
            case MONTH:
                return "date_part('month'," + args.get(0) + ")";
            case DAY:
                return "date_part('day'," + args.get(0) + ")";
            case HOUR:
                return "date_part('hour'," + args.get(0) + ")";
            case MINUTE:
                return "date_part('minute'," + args.get(0) + ")";
            case SECOND:
                return "date_part('second'," + args.get(0) + ")";
            case FRACTIONALSECONDS:
                break;
            case TOTALSECONDS:
                break;
            case DATE:
                break;
            case TIME:
                break;
            case TOTALOFFSETMINUTES:
                break;
            case MINDATETIME:
                break;
            case MAXDATETIME:
                break;
            case NOW:
                return " now() ";
            case ROUND:
                return "round(" + args.get(0) + ")";
            case FLOOR:
                return "floor(" + args.get(0) + ")";
            case CEILING:
                return "ceiling(" + args.get(0) + ")";
            case GEODISTANCE:
                break;
            case GEOLENGTH:
                break;
            case GEOINTERSECTS:
                break;
            case CAST:
                break;
            case ISOF:
                break;
        }
        return null;
    }

    @Override
    public Object visitLambdaExpression(String lambdaFunction, String lambdaVariable, Expression expression) throws ExpressionVisitException, ODataApplicationException {
        return null;
    }

    @Override
    public Object visitLiteral(String literal) throws ExpressionVisitException, ODataApplicationException {
        return literal;
    }

    ;

    @Override
    public Object visitMember(UriInfoResource member) throws ExpressionVisitException, ODataApplicationException {
        List<UriResource> resourcePaths = member.getUriResourceParts();
        UriResourceProperty uriResourceProperty = (UriResourceProperty) resourcePaths.get(0);
        return " t.\"data\"->>'" + uriResourceProperty.getProperty().getName() + "'";
    }

    @Override
    public Object visitAlias(String aliasName) throws ExpressionVisitException, ODataApplicationException {
        return null;
    }

    @Override
    public Object visitTypeLiteral(EdmType type) throws ExpressionVisitException, ODataApplicationException {
        return null;
    }

    @Override
    public Object visitLambdaReference(String variableName) throws ExpressionVisitException, ODataApplicationException {
        return null;
    }

    @Override
    public Object visitEnum(EdmEnumType type, List enumValues) throws ExpressionVisitException, ODataApplicationException {
        for (Object enumValue : enumValues) {
            EdmMember edmMember = type.getMember(enumValue.toString());
            if (edmMember == null)
                throw new ODataApplicationException(enumValue + " is not a valid enum member", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
            return "'" + edmMember.getValue() + "'";
        }

        return null;
    }
}
