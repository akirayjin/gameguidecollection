package com.akirayjin.gameguide.data

object SampleData {
    suspend fun populateSampleGuidesIfEmpty(dao: GameDao) {
        if (dao.getGameCount() > 0) return

        // 1. Persona 4 Golden
        val p4gId = dao.insertGame(
            GameEntity(
                title = "Persona 4 Golden",
                platform = "PS Vita",
                summary = "Classroom answers, Social Link schedule, True Ending requirements, and dungeon boss strategies.",
                updatedAt = System.currentTimeMillis()
            )
        )

        dao.insertSection(
            GuideSectionEntity(
                gameId = p4gId,
                title = "Classroom Answers Cheat Sheet",
                content = """
                    <h3>April Answers</h3>
                    <ul>
                        <li><b>4/14:</b> What is the year before 1 AD called? &rarr; <i>1 BC</i></li>
                        <li><b>4/18:</b> Bet you don't know who invented the alphabet? &rarr; <i>The Patriarchs</i></li>
                        <li><b>4/20:</b> How many colors are on a standard TV? &rarr; <i>Three</i></li>
                        <li><b>4/23:</b> What month gets its name from a goddess? &rarr; <i>May</i></li>
                        <li><b>4/26:</b> Which marriage was called 'Property Marriage'? &rarr; <i>To establish trade</i></li>
                    </ul>
                    <h3>May Answers</h3>
                    <ul>
                        <li><b>5/7:</b> What is 'the moon is beautiful' a romantic translation of? &rarr; <i>I love you</i></li>
                        <li><b>5/26:</b> Which type of poll is most accurate? &rarr; <i>Random sampling</i></li>
                    </ul>
                    <div class="note-box">
                        <b>Tip:</b> Max out Marie (Empress) Social Link to Rank 10 before December 21st to unlock the Hollow Forest Bonus Dungeon!
                    </div>
                """.trimIndent(),
                orderIndex = 1
            )
        )

        dao.insertSection(
            GuideSectionEntity(
                gameId = p4gId,
                title = "True Ending & Golden Ending Guide",
                content = """
                    <h3>December 3rd Choices (CRITICAL!)</h3>
                    <p>When in Namatame's room, choose these exact responses to avoid a Bad Ending:</p>
                    <ol>
                        <li>"Wait a second here..."</li>
                        <li>"We're missing something."</li>
                        <li>"Namatame's true feelings."</li>
                        <li>"Something's bothering me."</li>
                        <li>"We're missing something..."</li>
                        <li>"Calm the hell down!"</li>
                    </ol>
                    <h3>March 20th Final Day</h3>
                    <p>Do NOT go home when prompted after talking to all your Rank 10 Social Links! Go to the <b>Junes Food Court</b> instead to unlock the True Final Boss (Izanami).</p>
                """.trimIndent(),
                orderIndex = 2
            )
        )

        // 2. Final Fantasy X HD Remaster
        val ffxId = dao.insertGame(
            GameEntity(
                title = "Final Fantasy X HD",
                platform = "PS Vita",
                summary = "Al Bhed Primers location list, Celestial Weapons guide, and Overdrive combinations.",
                updatedAt = System.currentTimeMillis()
            )
        )

        dao.insertSection(
            GuideSectionEntity(
                gameId = ffxId,
                title = "Missable Al Bhed Primers",
                content = """
                    <div class="warning-box">
                        <b>Warning:</b> 4 Al Bhed Primers are MISSABLE and cannot be retrieved if passed!
                    </div>
                    <table>
                        <thead>
                            <tr><th>Volume</th><th>Location</th><th>Missable Point</th></tr>
                        </thead>
                        <tbody>
                            <tr><td>Primer XIX</td><td>Home - Entrance</td><td>Before leaving Home</td></tr>
                            <tr><td>Primer XX</td><td>Home - Living Quarters</td><td>Before leaving Home</td></tr>
                            <tr><td>Primer XXI</td><td>Home - Main Corridor</td><td>Before leaving Home</td></tr>
                            <tr><td>Primer XXII</td><td>Bevelle Temple</td><td>Right after marriage scene</td></tr>
                        </tbody>
                    </table>
                """.trimIndent(),
                orderIndex = 1
            )
        )

        dao.insertSection(
            GuideSectionEntity(
                gameId = ffxId,
                title = "Rikku Mix Combinations",
                content = """
                    <h3>Top Combat Mixes</h3>
                    <ul>
                        <li><b>Hyper Mighty G:</b> Healing Water + Dark Matter (Auto-Life, Protect, Shell, Haste, Regen on all)</li>
                        <li><b>Trio of 9999:</b> Dark Matter + Master Sphere (All attacks/heals deal 9999)</li>
                        <li><b>Ultra NulAll:</b> Healing Spring + Healing Spring (Nullifies all elements & boosts Stats)</li>
                    </ul>
                """.trimIndent(),
                orderIndex = 2
            )
        )

        // 3. Monster Hunter Freedom Unite
        val mhfuId = dao.insertGame(
            GameEntity(
                title = "Monster Hunter Freedom Unite",
                platform = "PS Vita / PSP",
                summary = "Elder Elder & Guild Key Quests list, weapon tree quick reference, and armor skills.",
                updatedAt = System.currentTimeMillis()
            )
        )

        dao.insertSection(
            GuideSectionEntity(
                gameId = mhfuId,
                title = "Elder Key Quests (Village 1★ to 6★)",
                content = """
                    <h3>1★ Village Elder</h3>
                    <ul>
                        <li>1★-3: Find the Legendary Chef</li>
                        <li>1★-4: Kill the Giadrome!</li>
                        <li>1★-5: Low Mountains Giant (Bulldrome)</li>
                    </ul>
                    <h3>2★ Village Elder</h3>
                    <ul>
                        <li>2★-8: The Secret Poison (Gypceros)</li>
                        <li>2★-11: Dear The Lush Jungle (Congalala)</li>
                        <li>2★-13: The Land Shark (Cephadrome)</li>
                        <li>2★-15: Lurking in the Desert (Hermitaur)</li>
                    </ul>
                    <h3>Urgent 2★ &rarr; 3★: The Shadow in the Snow (Khezu)</h3>
                """.trimIndent(),
                orderIndex = 1
            )
        )
    }
}
