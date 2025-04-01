package io.github.marcusmiao.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
  public static final Map<String, TokenType> KEY_WORDS;
  static {
    KEY_WORDS = new HashMap<>();
    KEY_WORDS.put("and", TokenType.AND);
    KEY_WORDS.put("class", TokenType.CLASS);
    KEY_WORDS.put("else", TokenType.ELSE);
    KEY_WORDS.put("false", TokenType.FALSE);
    KEY_WORDS.put("for", TokenType.FOR);
    KEY_WORDS.put("fun", TokenType.FUN);
    KEY_WORDS.put("if", TokenType.IF);
    KEY_WORDS.put("nil", TokenType.NIL);
    KEY_WORDS.put("or", TokenType.OR);
    KEY_WORDS.put("print", TokenType.PRINT);
    KEY_WORDS.put("return", TokenType.RETURN);
    KEY_WORDS.put("super", TokenType.SUPER);
    KEY_WORDS.put("this", TokenType.THIS);
    KEY_WORDS.put("true", TokenType.TRUE);
    KEY_WORDS.put("var", TokenType.VAR);
    KEY_WORDS.put("while", TokenType.WHILE);
  }

  private final String source;
  private final List<Token> tokens = new ArrayList<>();

  private int start = 0;
  private int current = 0;
  private int line = 1;

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
      if (Lox.hadError) {
        return tokens;
      }
    }
    tokens.add(new Token(TokenType.EOF, "", null, line));
    return tokens;
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(':
        addToken(TokenType.LEFT_PAREN);
        break;
      case ')':
        addToken(TokenType.RIGHT_PAREN);
        break;
      case '{':
        addToken(TokenType.LEFT_BRACE);
        break;
      case '}':
        addToken(TokenType.RIGHT_BRACE);
        break;
      case ',':
        addToken(TokenType.COMMA);
        break;
      case '.':
        addToken(TokenType.DOT);
        break;
      case '-':
        addToken(TokenType.MINUS);
        break;
      case '+':
        addToken(TokenType.PLUS);
        break;
      case ';':
        addToken(TokenType.SEMICOLON);
        break;
      case '*':
        addToken(TokenType.STAR);
        break;
      case '!':
        addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
        break;
      case '=':
        addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
        break;
      case '<':
        addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
        break;
      case '>':
        addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
        break;
      case '/':
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd()) {
            advance();
          }
        } else if (match('*')) {
          int nestLevel = 1;
          while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
              advance();
              advance();
              nestLevel--;
              if (nestLevel == 0) {
                break;
              }
              continue;
            }
            if (peek() == '/' && peekNext() == '*') {
              advance();
              advance();
              nestLevel++;
              continue;
            }
            if (peek() == '\n') {
              line++;
            }
            advance();
          }
          if (nestLevel != 0) {
            Lox.error(line, "Multiline comment doesn't terminate properly");
          }
        } else {
          addToken(TokenType.SLASH);
        }
        break;
      case ' ':
      case '\t':
      case '\r':
        break;
      case '\n':
        line++;
        break;
      case '"':
        string();
        break;
      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character '" + c + "'");
        }
        break;
    }
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  private boolean match(char expected) {
    if (isAtEnd()) {
      return false;
    }
    if (source.charAt(current) != expected) {
      return false;
    }
    current++;
    return true;
  }

  private char peek() {
    if (isAtEnd()) {
      return '\0';
    }
    return source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length()) {
      return '\0';
    }
    return source.charAt(current + 1);
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') {
        line++;
      }
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unexpected end of string");
      return;
    }

    // Consumes the closing double quotation mark
    advance();

    String value = source.substring(start + 1, current - 1);
    addToken(TokenType.STRING, value);
  }

  private void number() {
    while (isDigit(peek())) {
      advance();
    }
    if (peek() == '.' && isDigit(peekNext())) {
      do {
        advance();
      } while (isDigit(peek()));
    }
    addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) {
      advance();
    }
    String text = source.substring(start, current);
    addToken(KEY_WORDS.getOrDefault(text, TokenType.IDENTIFIER));
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }
}
