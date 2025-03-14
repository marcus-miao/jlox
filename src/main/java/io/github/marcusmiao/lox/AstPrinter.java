package io.github.marcusmiao.lox;

import io.github.marcusmiao.lox.Expr.Binary;
import io.github.marcusmiao.lox.Expr.Grouping;
import io.github.marcusmiao.lox.Expr.Literal;
import io.github.marcusmiao.lox.Expr.Unary;

class AstPrinter implements Expr.Visitor<String> {

  public String print(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String visitBinaryExpr(Binary expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Literal expr) {
    if (expr.value == null) {
      return "nil";
    }
    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  private String parenthesize(String name, Expr... exprs) {
    StringBuilder sb = new StringBuilder();
    sb.append("(").append(name);
    for (Expr expr : exprs) {
      sb.append(" ");
      sb.append(expr.accept(this));
    }
    sb.append(")");
    return sb.toString();
  }

  public static void main(String[] args) {
    Expr expression = new Expr.Binary(
        new Expr.Unary(
            new Token(TokenType.MINUS, "-", null, 1),
            new Expr.Literal(123)),
        new Token(TokenType.STAR, "*", null, 1),
        new Expr.Grouping(
            new Expr.Literal(45.67)));

    System.out.println(new AstPrinter().print(expression));
  }
}
