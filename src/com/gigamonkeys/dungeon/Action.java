package com.gigamonkeys.dungeon;

import static com.gigamonkeys.dungeon.Text.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * An action that can occur in a turn, e.g. a monster attacks a player.
 */
public interface Action {
  /**
   * The description of the action happening plus the immediate result. e.g.
   * "You swing your axe and hit, leaving a nasty gash in the Blobbyblob."
   */
  public String description();

  /**
   * Dispatch this action to the appropriate event handler on the Thing.
   */
  public default Stream<Action> event(Thing t) {
    return Stream.empty();
  }

  //////////////////////////////////////////////////////////////////////////////
  // Non event generating pseudo actions.

  // When we just want to generate a single response use this as it generates no
  // action events.
  public static Action none(String description) {
    return () -> description;
  }

  // When we want to generate a single response and short circuit other the rest
  // of the command invocation, including the automatic wrapping of output.
  public static Action noWrap(String description) {
    return () -> {
      throw new Command.SpecialOutput(description);
    };
  }

  //////////////////////////////////////////////////////////////////////////////
  // Static methods for creating Actions. (Return type is just Action rather
  // than more specific type so make generic Optionals and Streams work better
  // different types of Action are combined.)

  public static Action attack(int damage, String text, Player p) {
    return new Attack(damage, text, p);
  }

  public static Action drop(Player p, Thing thing) {
    return new Drop(p, thing);
  }

  public static Action eat(Player p, Thing t) {
    return new Eat(p, t);
  }

  public static Action go(Player p, Door door) {
    return new Go(p, door);
  }

  public static Action look(Player p) {
    return new Look(p);
  }

  public static Action move(Thing t, Location l, String p, String d) {
    return new Move(t, l, p, d);
  }

  public static Action playerAttack(Thing monster, Thing weapon) {
    return new PlayerAttack(monster, weapon);
  }

  public static Action say(Thing who, String what) {
    return new Say(who, what);
  }

  public static Action take(Player p, List<Thing> things) {
    return new Take(p, things);
  }

  //////////////////////////////////////////////////////////////////////////////
  // Concrete actions. All actions that generate events need to have a concrete
  // class rather than a lambda.

  public static record Attack(int damage, String text, Player player) implements Action {
    public String description() {
      player.takeDamage(damage);
      return text;
    }

    public Stream<Action> event(Thing t) {
      return t.onAttack(this);
    }
  }

  public static record Drop(Player player, Thing thing) implements Action {
    public String description() {
      player.drop(thing);
      return "You drop the " + thing.name() + ".";
    }

    public Stream<Action> event(Thing t) {
      return t.onDrop(this);
    }
  }

  public static record Eat(Player player, Thing food) implements Action {
    public String description() {
      return food.eat();
    }
    public Stream<Action> event(Thing t) {
      return t.onEat(this);
    }
  }

  public static record Go(Player player, Door door) implements Action {
    public String description() {
      player.go(door);
      return player.room().description();
    }

    public Stream<Action> event(Thing t) {
      return t.onEnter(this);
    }
  }

  public static record Look(Player player) implements Action {
    public String description() {
      return player.room().description();
    }
    public Stream<Action> event(Thing t) {
      return t.onLook(this);
    }
  }

  public static record Move(Thing thing, Location location, String place, String movement) implements Action {
    public String description() {
      location.placeThing(thing, place);
      return movement;
    }

    public Stream<Action> event(Thing t) {
      return t.onMove(this);
    }
  }

  public static record PlayerAttack(Thing monster, Thing weapon) implements Action {
    public String description() {
      var attack = weapon.attack();
      return attack.description() + " " + attack.result(monster);
    }

    public Stream<Action> event(Thing t) {
      return t.onPlayerAttack(this);
    }
  }

  public static record Say(Thing speaker, String what) implements Action {
    public String description() {
      return "'" + what + "' says the " + speaker.name() + ".";
    }

    public Stream<Action> event(Thing t) {
      return t.onSay(this);
    }
  }

  public static record Take(Player p, List<Thing> things) implements Action {
    public String description() {
      var taken = new ArrayList<String>();
      var notTaken = new ArrayList<String>();
      for (var t : things) {
        if (t.canBeTaken()) {
          p.placeThing(t, "in your stuff");
          taken.add(t.description());
        } else {
          notTaken.add(t.name());
        }
      }
      var desc = new ArrayList<String>();
      if (!taken.isEmpty()) {
        desc.add("Okay, took " + commify(taken) + ".");
      }
      if (!notTaken.isEmpty()) {
        desc.add("Can't take " + commify(notTaken) + ".");
      }
      return String.join(" ", desc);
    }

    public Stream<Action> event(Thing t) {
      return t.onTake(this);
    }
  }

  public static record Turn(Player player) implements Action {
    public String description() {
      return ""; // This is a pseudo action so no description.
    }

    public Stream<Action> event(Thing t) {
      return t.onTurn(this);
    }
  }
}
