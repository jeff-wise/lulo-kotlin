
package data.rpg


import effect.Nothing



val silaValue =
    Character(
        "Sila",
        "elf",
        Class("Wizard", true, Nothing()),
        listOf(
            Item.Weapon("Staff", 10)
        ,   Item.Potion("Healing", 7.5)
        )
    )

