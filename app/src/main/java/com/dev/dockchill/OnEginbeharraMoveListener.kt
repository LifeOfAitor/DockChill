package com.dev.dockchill

/**
 * Eginbeharra (task) baten posizioa aldatu behar denean erabiltzen den interface-a.
 */
interface OnEginbeharraMoveListener {
    fun onMoveUp(position: Int)
    fun onMoveDown(position: Int)
}