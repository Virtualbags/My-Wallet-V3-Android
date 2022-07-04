package com.blockchain.presentation

import com.blockchain.commonarch.presentation.mvi_v2.compose.ComposeNavigationDestination

sealed class BackPhraseDestination(override val route: String) : ComposeNavigationDestination {
    object BackedUpPhrase : BackPhraseDestination("BackedUpPhrase")
    object BackupPhraseIntro : BackPhraseDestination("BackupPhraseIntro")
    object RecoveryPhrase : BackPhraseDestination("RecoveryPhrase")
    object ManualBackup : BackPhraseDestination("ManualBackup")
    object VerifyPhrase : BackPhraseDestination("VerifyPhrase")
    object BackupSuccess : BackPhraseDestination("BackupSuccess")
}
