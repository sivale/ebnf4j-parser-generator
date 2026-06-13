package com.sverko.ebnf.result;

import java.util.Objects;

public class TriviaResultNode extends BaseResultNode {

  private final String category;

  public TriviaResultNode(String category, int fromToken, int toToken) {
    this.category = Objects.requireNonNull(category, "category must not be null");
    this.name = "trivia";
    this.foundToken = fromToken;
    setSpan(fromToken, toToken);
  }

  public String getCategory() {
    return category;
  }

  @Override
  public ResultNodeType getType() {
    return ResultNodeType.TRIVIA;
  }
}
