package com.gigamonkeys.dungeon;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Infrastructure for parsing commands from strings as tokens.
 */
public record CommandParser(Player player) {
  /**
   * Usual entry point, generates a Parse of the given element of args if it
   * exists and an error parse otherwise.
   */
  public static Parse<String, String[]> arg(String[] args, int idx) {
    return idx < args.length ? good(args[idx], args) : bad(args, null);
  }

  /**
   * Combine consecutive tokens into a single argument.
   */
  public static Parse<String, String[]> args(String[] args, int start, int end) {
    var s = String.join(" ", Arrays.asList(Arrays.copyOfRange(args, start, end)));
    return !s.equals("") ? good(s, args) : bad(args, null);
  }

  public static <T> Parse<T, Object> implicit(Supplier<Optional<T>> opt) {
    return opt.get().map(t -> good(t, null)).orElse(bad(null, null));
  }

  /**
   * Interface for command arg parsing. Needed since we need to throw an
   * exception.
   */
  @FunctionalInterface
  public static interface Parser {
    public Action parse(String[] args) throws BadCommandException;
  }

  /**
   * Interface for finally converting to an action which is where the
   * BadCommandException is actually thrown. Needed for the case where we chain
   * multiple Parse<> objects via lambdas.
   */
  @FunctionalInterface
  public static interface ToAction<T> {
    public Action actionify(T value) throws BadCommandException;
  }

  public static class BadCommandException extends Exception {

    BadCommandException(String message) {
      super(message);
    }
  }

  public static interface Parse<T, U> {
    /**
     * Succeed if we can convert the current value to a new value.
     */
    public <X> Parse<X, T> maybe(Function<T, Optional<X>> fn);

    /**
     * Set the error message if we have failed.
     */
    public Parse<T, U> or(String error);

    /**
     * Set the error message based on the previous value if we have failed.
     */
    public Parse<T, U> or(Function<U, String> error);

    /**
     * Expect a specific value and fail otherwise.
     */
    public default Parse<T, T> expect(T expected) {
      return maybe(v -> Optional.ofNullable(expected.equals(v) ? v : null));
    }

    /**
     * If we are a good parse, convert the value to an Action, throwing
     * BadCommandException if we cannot.
     */
    public Action toAction(ToAction<T> fn) throws BadCommandException;
  }

  public static <T, U> Parse<T, U> good(T v, U p) {
    return new Good<>(v, p);
  }

  public static <T, U> Parse<T, U> bad(U p, String s) {
    return new Bad<>(p, s);
  }

  private static record Good<T, U>(T value, U previous) implements Parse<T, U> {
    public <X> Parse<X, T> maybe(Function<T, Optional<X>> fn) {
      return fn.apply(value).map(x -> good(x, value)).orElse(bad(value, null));
    }

    public Parse<T, U> or(String error) {
      return this;
    }

    public Parse<T, U> or(Function<U, String> error) {
      return this;
    }

    public Action toAction(ToAction<T> fn) throws BadCommandException {
      return fn.actionify(value);
    }

    public String toString() {
      return "Good parse: value: " + value + "; previous: " + previous;
    }
  }

  private static record Bad<T, U>(U previous, String error) implements Parse<T, U> {
    public <X> Parse<X, T> maybe(Function<T, Optional<X>> fn) {
      return bad(null, error);
    }

    public Parse<T, U> or(String error) {
      return bad(previous, error);
    }

    public Parse<T, U> or(Function<U, String> error) {
      return bad(previous, error.apply(previous));
    }

    public Action toAction(ToAction<T> fn) throws BadCommandException {
      throw new BadCommandException(error);
    }

    public String toString() {
      return "Bad parse: previous: " + previous + "; error: " + error;
    }
  }
}
