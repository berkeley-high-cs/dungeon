package com.gigamonkeys.dungeon;

public class Blobbyblob extends BaseThing {

  Blobbyblob(int hitPoints) {
    super("Blobbyblob", hitPoints, 2);
  }

  public boolean isMonster() {
    return true;
  }

  public boolean isEdible() {
    return !alive();
  }

  public String attackWith(int damage) {
    takeDamage(damage);
    if (alive()) {
      return "The " + name() + " is wounded but still alive. And now it's mad.";
    } else {
      return "The " + name() + " is dead. Murderer.";
    }
  }

  public String where() {
    return "across from you";
  }

  public String description() {
    if (alive()) {
      return (name() + ", a gelatenous mass with too many eyes and an odor of jello casserole gone bad");
    } else {
      return "dead " + name() + " decaying into puddle of goo";
    }
  }

  public String eat() {
    if (alive()) {
      return "Are you out of your mind?! This is a live and jiggling BlobbyBlob!";
    } else {
      return "Ugh. This is worse than the worst jello casserole you have ever tasted. But it does slightly sate your hunger.";
    }
  }

  public Attack attackPlayer() {
    if (alive()) {
      return new Attack(damage(), "The " + name() + " extrudes a blobby arm and smashes at you!");
    } else {
      return Attack.EMPTY;
    }
  }
}
