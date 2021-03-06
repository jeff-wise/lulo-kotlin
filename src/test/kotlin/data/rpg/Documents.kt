
package data.rpg


import lulo.document.*


// SILA
// ---------------------------------------------------------------------------------------------

val silaDocYaml =
    """
    name: Sila
    race: elf
    class:
      name: Wizard
      uses_magic: true
    inventory:
    - type: weapon
      weapon:
        name: Staff
        damage: 10
    - type: potion
      potion:
        name: Healing
        price: 7.5
    """


val silaDocObject =
    DocDict(
        mapOf(
            "name" to DocText("Sila", DocPath(listOf(DocKeyNode("name")))),
            "race" to DocText("elf", DocPath(listOf(DocKeyNode("race")))),
            "class" to DocDict(
                mapOf(
                    "name" to DocText("Wizard",
                                      DocPath(listOf(DocKeyNode("class"), DocKeyNode("name")))),
                    "uses_magic" to DocBoolean(true,
                                               DocPath(listOf(DocKeyNode("class"), DocKeyNode("uses_magic"))))
                ),
                listOf(),
                DocPath(listOf(DocKeyNode("class")))
            ),
            "inventory" to DocList(
                listOf(
                    DocDict(
                        mapOf(
                            "name" to DocText("Staff",
                                              listOf(),
                                              DocPath(listOf(DocKeyNode("inventory"),
                                                             DocIndexNode(0),
                                                             DocKeyNode("name")))),
                            "damage" to DocNumber(10.0,
                                                  listOf(),
                                                  DocPath(listOf(DocKeyNode("inventory"),
                                                                 DocIndexNode(0),
                                                                 DocKeyNode("damage"))))
                        ),
                        listOf("weapon"),
                        DocPath(listOf(DocKeyNode("inventory"), DocIndexNode(0)))
                    ),
                    DocDict(
                        mapOf(
                            "name" to DocText("Healing",
                                              listOf(),
                                              DocPath(listOf(DocKeyNode("inventory"),
                                                             DocIndexNode(1),
                                                             DocKeyNode("name")))),
                            "price" to DocNumber(7.5,
                                                listOf(),
                                                DocPath(listOf(DocKeyNode("inventory"),
                                                               DocIndexNode(1),
                                                               DocKeyNode("price"))))
                        ),
                        listOf("potion"),
                        DocPath(listOf(DocKeyNode("inventory"), DocIndexNode(1)))
                    )
                ),
                DocPath(listOf(DocKeyNode("inventory")))
            )
        ),
        listOf(),
        DocPath(listOf())
    )



// SILA MISSING WEAPON NAME
// ---------------------------------------------------------------------------------------------

val silaMissingWeaponNameDocYaml =
    """
    name: Sila
    race: elf
    class:
      name: Wizard
      uses_magic: true
    inventory:
    - type: weapon
      weapon:
        damage: 10
    - type: potion
      potion:
        name: Healing
        price: 7.5
    """


// SILA MISSING WEAPON NAME
// ---------------------------------------------------------------------------------------------

val silaWrongCaseTypeDocYaml =
    """
    name: Sila
    race: elf
    class:
      name: Wizard
      uses_magic: true
    inventory:
    - type: race
      race: elf
    - type: potion
      potion:
        name: Healing
        price: 7.5
    """

