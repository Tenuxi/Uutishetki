package fi.uutishetki

import org.junit.Assert.*
import org.junit.Test

class AppearanceTest {
    @Test fun missingOrUnknownSavedChoiceFollowsSystem() {
        assertEquals(Appearance.SYSTEM, Appearance.decode(null))
        assertEquals(Appearance.SYSTEM, Appearance.decode("unknown"))
        assertFalse(Appearance.decode(null).isDark(false))
        assertTrue(Appearance.decode(null).isDark(true))
    }
    @Test fun storedNamesRoundTripAndExplicitChoiceOverridesSystem() {
        Appearance.entries.forEach { assertEquals(it, Appearance.decode(it.name)) }
        assertFalse(Appearance.LIGHT.isDark(true))
        assertFalse(Appearance.LIGHT.isDark(false))
        assertTrue(Appearance.DARK.isDark(false))
        assertTrue(Appearance.DARK.isDark(true))
    }
    @Test fun sameSystemChoiceRespondsToSystemThemeChanges() {
        val selected = Appearance.decode("SYSTEM")
        assertFalse(selected.isDark(false))
        assertTrue(selected.isDark(true))
        assertFalse(selected.isDark(false))
    }
}
