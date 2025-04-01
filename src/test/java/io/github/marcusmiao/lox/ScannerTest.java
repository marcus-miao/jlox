package io.github.marcusmiao.lox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ScannerTest {
  @BeforeEach
  public void setUp() {
    Lox.hadError = false;
  }

  @Test
  public void testUnaryOperators() {
    Scanner scanner = new Scanner("(){},.-+;*=<>/!");
    List<Token> tokens = scanner.scanTokens();
    List<TokenType> expectedTypes = new ArrayList<TokenType>(){{
      add(TokenType.LEFT_PAREN);
      add(TokenType.RIGHT_PAREN);
      add(TokenType.LEFT_BRACE);
      add(TokenType.RIGHT_BRACE);
      add(TokenType.COMMA);
      add(TokenType.DOT);
      add(TokenType.MINUS);
      add(TokenType.PLUS);
      add(TokenType.SEMICOLON);
      add(TokenType.STAR);
      add(TokenType.EQUAL);
      add(TokenType.LESS);
      add(TokenType.GREATER);
      add(TokenType.SLASH);
      add(TokenType.BANG);
      add(TokenType.EOF);
    }};
    assertEquals(expectedTypes.size(), tokens.size());
    for (int i = 0; i < expectedTypes.size(); i++) {
      assertEquals(expectedTypes.get(i), tokens.get(i).type);
    }
  }

  @Test
  public void testLookAheadMatch() {
    Scanner scanner = new Scanner(">= <= == !=");
    List<Token> tokens = scanner.scanTokens();
    List<TokenType> expectedTypes = new ArrayList<TokenType>(){{
      add(TokenType.GREATER_EQUAL);
      add(TokenType.LESS_EQUAL);
      add(TokenType.EQUAL_EQUAL);
      add(TokenType.BANG_EQUAL);
      add(TokenType.EOF);
    }};
    assertEquals(expectedTypes.size(), tokens.size());
    for (int i = 0; i < expectedTypes.size(); i++) {
      assertEquals(expectedTypes.get(i), tokens.get(i).type);
    }
  }

  @Test
  public void testIgnoreWhiteSpace() {
    Scanner scanner = new Scanner("\t\r \t\r\r ");
    List<Token> tokens = scanner.scanTokens();
    List<TokenType> expectedTypes = new ArrayList<TokenType>(){{
      add(TokenType.EOF);
    }};
    assertEquals(expectedTypes.size(), tokens.size());
    for (int i = 0; i < expectedTypes.size(); i++) {
      assertEquals(expectedTypes.get(i), tokens.get(i).type);
    }
  }

  @Test
  public void testNewLine() {
    Scanner scanner = new Scanner("\n\n\n");
    List<Token> tokens = scanner.scanTokens();
    assertEquals(tokens.size(), 1);
    assertEquals(TokenType.EOF, tokens.get(0).type);
    assertEquals(4, tokens.get(0).line);
  }

  @Test
  public void testComment() {
    Scanner scanner = new Scanner("//This is a comment line which should be ignored\n");
    List<Token> tokens = scanner.scanTokens();
    assertEquals(tokens.size(), 1);
    assertEquals(TokenType.EOF, tokens.get(0).type);
    assertEquals(2, tokens.get(0).line);
  }

  @Test
  public void testKeywords() {
    StringBuilder sb = new StringBuilder();
    for (String keyword : Scanner.KEY_WORDS.keySet()) {
      sb.append(keyword);
      sb.append("\n");
    }
    Scanner scanner = new Scanner(sb.toString());
    List<Token> tokens = scanner.scanTokens();
    assertFalse(tokens.isEmpty());
    assertEquals(tokens.get(tokens.size() - 1).type, TokenType.EOF);
    tokens.remove(tokens.size() - 1);
    for (Token token : tokens) {
      assertTrue(Scanner.KEY_WORDS.containsKey(token.lexeme));
      assertEquals(Scanner.KEY_WORDS.get(token.lexeme), token.type);
    }
  }

  @Test
  public void testMaxMunch() {
    StringBuilder sb = new StringBuilder();
    for (String keyword : Scanner.KEY_WORDS.keySet()) {
      sb.append(keyword);
      sb.append("abcdefg\n");
    }
    Scanner scanner = new Scanner(sb.toString());
    List<Token> tokens = scanner.scanTokens();
    assertEquals(tokens.size(), Scanner.KEY_WORDS.keySet().size() + 1);
    tokens.remove(tokens.size() - 1);
    for (Token token : tokens) {
      assertFalse(Scanner.KEY_WORDS.containsKey(token.lexeme));
      assertEquals(TokenType.IDENTIFIER, token.type);
    }
  }

  @Test
  public void testSingleLineString() {
    String[] strings = {"abc", "Hello World", "Crafting Interpreters!", "Coding..."};
    StringBuilder sb = new StringBuilder();
    for (String string : strings) {
      sb.append("\"");
      sb.append(string);
      sb.append("\"");
    }
    Scanner scanner = new Scanner(sb.toString());
    List<Token> tokens = scanner.scanTokens();
    assertEquals(tokens.size() - 1, strings.length);
    assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).type);
    tokens.remove(tokens.size() - 1);
    for (int i = 0; i < strings.length; i++) {
      assertEquals(TokenType.STRING, tokens.get(i).type);
      assertEquals(strings[i], tokens.get(i).literal);
    }
  }

  @Test
  public void testMultiLineString() {
    String code = "\"Hello World\nCrafting Interpreters!\"";
    Scanner scanner = new Scanner(code);
    List<Token> tokens = scanner.scanTokens();
    assertEquals(tokens.size(), 2);
    assertEquals(TokenType.STRING, tokens.get(0).type);
    assertEquals(code, tokens.get(0).lexeme);

    assertEquals(TokenType.EOF, tokens.get(1).type);
    assertEquals(2, tokens.get(1).line);
  }

  @Test
  public void testInvalidString() {
    Scanner scanner = new Scanner("\"abc");
    scanner.scanTokens();
    assertTrue(Lox.hadError);
  }

  @Test
  public void testNumbers() {
    String[] numberStrings = {"10", "10.1", "10.234", "0.375", "9000"};
    StringBuilder sb = new StringBuilder();
    for (String string : numberStrings) {
      sb.append(string);
      sb.append("\n");
    }
    Scanner scanner = new Scanner(sb.toString());
    List<Token> tokens = scanner.scanTokens();
    assertEquals(tokens.size() - 1, numberStrings.length);
    assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).type);
    tokens.remove(tokens.size() - 1);
    for (int i = 0; i < numberStrings.length; i++) {
      assertEquals(TokenType.NUMBER, tokens.get(i).type);
      assertEquals(Double.parseDouble(numberStrings[i]), tokens.get(i).literal);
    }
  }

  @Test
  public void testIdentifiers() {
    String[] identifiers = {"my_var", "my_var_1", "MyVar", "My_Var936", "my_Var377"};
    StringBuilder sb = new StringBuilder();
    for (String identifier : identifiers) {
      sb.append(identifier);
      sb.append("\n");
    }
    Scanner scanner = new Scanner(sb.toString());
    List<Token> tokens = scanner.scanTokens();
    assertEquals(tokens.size() - 1, identifiers.length);
    assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).type);
    tokens.remove(tokens.size() - 1);
    for (int i = 0; i < identifiers.length; i++) {
      assertEquals(TokenType.IDENTIFIER, tokens.get(i).type);
      assertEquals(identifiers[i], tokens.get(i).lexeme);
    }
  }

  @Test
  public void testMultilineComment() {
    Scanner scanner = new Scanner("/*/*/*//This is a valid\nmultiline comment*/*/*/");
    List<Token> tokens = scanner.scanTokens();
    assertEquals(tokens.size(), 1);
    assertEquals(TokenType.EOF, tokens.get(0).type);
    assertEquals(2, tokens.get(0).line);
    assertFalse(Lox.hadError);

    scanner = new Scanner("/*/*This is an invalid\nmultiline comment*/");
    scanner.scanTokens();
    assertTrue(Lox.hadError);
  }
}
