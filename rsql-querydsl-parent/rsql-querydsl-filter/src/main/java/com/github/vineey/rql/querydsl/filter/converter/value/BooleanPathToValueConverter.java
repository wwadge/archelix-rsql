/* * MIT License
 *  * Copyright (c) 2016 John Michael Vincent S. Rustia
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
*  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE. *  */
package com.github.vineey.rql.querydsl.filter.converter.value;

import com.github.vineey.rql.querydsl.filter.QuerydslFilterParam;
import com.github.vineey.rql.querydsl.filter.UnsupportedRqlOperatorException;
import com.github.vineey.rql.querydsl.filter.converter.ConverterConstant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.NullExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;

import static com.querydsl.core.types.dsl.Expressions.asBoolean;
import static com.querydsl.core.types.dsl.Expressions.nullExpression;
import static cz.jirutka.rsql.parser.ast.RSQLOperators.EQUAL;
import static cz.jirutka.rsql.parser.ast.RSQLOperators.NOT_EQUAL;

/**
 * @author vrustia on 10/10/2015.
 */
public class BooleanPathToValueConverter implements PathToValueConverter<BooleanPath> {
    @Override
    public BooleanExpression evaluate(BooleanPath path, ComparisonNode comparisonNode, QuerydslFilterParam param) {
        Expression arg = convertToExpression(comparisonNode, param);
        ComparisonOperator operator = comparisonNode.getOperator();

        if (arg == null || arg instanceof NullExpression) {
            return path.isNull();
        } else {
            if (EQUAL.equals(operator)) {
                return path.eq(arg);
            } else if (NOT_EQUAL.equals(operator)) {
                return path.ne(arg).or(path.isNull());
            }
        }

        throw new UnsupportedRqlOperatorException(comparisonNode, path.getClass());
    }

    private Expression convertToExpression(ComparisonNode comparisonNode, QuerydslFilterParam param) {
        String arg = comparisonNode.getArguments().get(0);

        Expression exp = nullExpression();

        if (arg != null) {
            Path rhsPath = param.getMapping().get(arg);

            if (rhsPath != null) {
                exp = asBoolean(rhsPath);
            } else if (!ConverterConstant.NULL.equalsIgnoreCase(arg)) {
                exp = asBoolean(Boolean.valueOf(arg));
            }

        }
        return exp;
    }

}
