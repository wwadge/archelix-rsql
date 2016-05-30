/*
* MIT License
*
* Copyright (c) 2016 John Michael Vincent S. Rustia
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package com.github.vineey.rql.querydsl.filter.converter.value;

import com.github.vineey.rql.querydsl.filter.QuerydslFilterParam;
import com.github.vineey.rql.querydsl.filter.UnsupportedRqlOperatorException;
import com.github.vineey.rql.querydsl.filter.converter.ConverterConstant;
import com.github.vineey.rql.querydsl.filter.util.Enums;
import com.google.common.collect.Lists;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.NullExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;

import java.util.List;

import static com.querydsl.core.types.dsl.Expressions.nullExpression;
import static cz.jirutka.rsql.parser.ast.RSQLOperators.*;

/**
 * @author vrustia on 9/26/2015.
 */
public class EnumPathToValueConverter implements PathToValueConverter<EnumPath> {

    @Override
    public BooleanExpression evaluate(EnumPath path, ComparisonNode comparisonNode, QuerydslFilterParam param) {
        List<Expression> arg = convertToExpression(path, comparisonNode, param);
        ComparisonOperator operator = comparisonNode.getOperator();

        switch (arg.size()) {
            case 0:
                return path.isNull();
            case 1:
                Expression firstArg = arg.get(0);
                if (EQUAL.equals(operator)) {
                    return (firstArg instanceof NullExpression) ? path.isNull() : path.eq(firstArg);
                } else if (NOT_EQUAL.equals(operator)) {
                    return (firstArg instanceof NullExpression) ? path.isNotNull() : path.ne(firstArg);
                }

                break;
            default:
                if (IN.equals(operator)) {
                    return path.in(arg);
                } else if (NOT_IN.equals(operator)) {
                    return path.notIn(arg);
                }

        }

        throw new UnsupportedRqlOperatorException(comparisonNode, path.getClass());
    }

    private List<Expression> convertToExpression(EnumPath path, ComparisonNode comparisonNode, QuerydslFilterParam param) {


        List<Expression> result = Lists.newArrayList();

        for (String arg : comparisonNode.getArguments()) {

            if (arg != null) {
                Path rhsPath = param.getMapping().get(arg);

                if (rhsPath != null) {
                    result.add(Expressions.asEnum(rhsPath));
                } else if (!ConverterConstant.NULL.equalsIgnoreCase(arg)) {
                    result.add(Expressions.asEnum((Enum) Enums.getEnum(path.getType(), arg)));
                } else {
                    result.add(nullExpression());
                }

            }
        }
        return result;
    }

}
