package com.theoplayer.android.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(InternalUiApi::class)
class UIControllerScopeImplTest {
    @Test
    fun `closeAllMenus closes all open menus`() {
        val scope = UIControllerScopeImpl(theoplayerView = null, providedPlayer = null)
        scope.openMenu {}
        scope.openMenu {}
        scope.openMenu {}

        scope.closeAllMenus()

        assertNull(scope.currentMenu)
        assertTrue(scope.menuIds.isEmpty())
        assertTrue(scope.lastWasClosed)
    }

    @Test
    fun `closeAllMenus on empty stack is a no-op`() {
        val scope = UIControllerScopeImpl(theoplayerView = null, providedPlayer = null)

        scope.closeAllMenus()

        assertNull(scope.currentMenu)
        assertTrue(scope.menuIds.isEmpty())
        assertFalse(scope.lastWasClosed)
    }

    @Test
    fun `closeCurrentMenu only closes the top menu`() {
        val scope = UIControllerScopeImpl(theoplayerView = null, providedPlayer = null)
        scope.openMenu {}
        scope.openMenu {}

        scope.closeCurrentMenu()

        assertEquals(1, scope.menuIds.size)
        assertTrue(scope.lastWasClosed)
    }
}
