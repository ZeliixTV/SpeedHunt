package com.speedhunt.game;

/** All 30 collectible skins. */
final class Skin {
    final String name, emoji, rarity;
    final int price;

    Skin(String name, String emoji, String rarity, int price) {
        this.name = name;
        this.emoji = emoji;
        this.rarity = rarity;
        this.price = price;
    }

    int color() {
        switch (rarity) {
            case "Legendary": return Pal.GOLD;
            case "Epic": return Pal.PURPLE;
            case "Rare": return Pal.MINT;
            default: return 0xFF9AA0B5;
        }
    }

    static final String[] RARITIES = {"Common", "Rare", "Epic", "Legendary"};

    static final Skin[] ALL = new Skin[]{
        new Skin("Mochi Duck", "🦆", "Common", 0), new Skin("Tiny Mouse", "🐭", "Common", 0),
        new Skin("Cloud Cat", "🐱", "Common", 25), new Skin("Bunny Pop", "🐰", "Common", 25),
        new Skin("Little Frog", "🐸", "Common", 25), new Skin("Panda Cub", "🐼", "Rare", 50),
        new Skin("Red Fox", "🦊", "Rare", 50), new Skin("Baby Bear", "🐻", "Rare", 50),
        new Skin("Chick", "🐤", "Rare", 50), new Skin("Penguin", "🐧", "Rare", 50),
        new Skin("Shiba", "🐕", "Epic", 100), new Skin("Raccoon", "🦝", "Epic", 100),
        new Skin("Koala", "🐨", "Epic", 100), new Skin("Tiger Cub", "🐯", "Epic", 100),
        new Skin("Little Dragon", "🐲", "Epic", 100), new Skin("Deer", "🦌", "Epic", 100),
        new Skin("Pink Axolotl", "🦎", "Epic", 100), new Skin("Owl", "🦉", "Epic", 100),
        new Skin("Star Spirit", "⭐", "Legendary", 200), new Skin("Moon Bunny", "🌙", "Legendary", 200),
        new Skin("Crystal Fox", "💎", "Legendary", 200), new Skin("Fire Kitty", "🔥", "Legendary", 200),
        new Skin("Cherry Ghost", "👻", "Legendary", 200), new Skin("Sun Sprite", "☀️", "Legendary", 200),
        new Skin("Lucky Panda", "🍀", "Legendary", 200), new Skin("Ninja Cat", "🐈", "Legendary", 200),
        new Skin("Pixel Heart", "💖", "Legendary", 200), new Skin("Comet", "☄️", "Legendary", 200),
        new Skin("Golden Duck", "🦆", "Legendary", 300), new Skin("Royal Dragon", "🐉", "Legendary", 300),
    };
}
