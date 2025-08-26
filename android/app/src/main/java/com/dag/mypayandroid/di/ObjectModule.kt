package com.dag.mypayandroid.di

import android.content.Context
import android.content.pm.PackageManager

import com.dag.mypayandroid.base.helper.system.ActivityHolder
import com.dag.mypayandroid.base.helper.system.AlertDialogManager
import com.dag.mypayandroid.base.helper.security.BiometricHelper
import com.dag.mypayandroid.base.helper.blockchain.SolanaHelper
import com.dag.mypayandroid.base.helper.blockchain.SolanaHelperImpl
import com.dag.mypayandroid.base.helper.blockchain.WalletManager
import com.dag.mypayandroid.base.helper.blockchain.Web3AuthHelper
import com.dag.mypayandroid.base.helper.blockchain.Web3AuthHelperImpl
import com.dag.mypayandroid.base.helper.security.NFCHelper
import com.dag.mypayandroid.base.navigation.DefaultNavigator
import com.dag.mypayandroid.base.navigation.Destination
import com.dag.mypayandroid.base.scroll.ScrollStateManager
import com.web3auth.core.Web3Auth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.sol4k.Connection
import javax.inject.Singleton
import org.sol4k.RpcUrl

@Module
@InstallIn(SingletonComponent::class)
class ObjectModules {

    @Provides
    @Singleton
    fun provideDefaultNavigator(): DefaultNavigator {
        return DefaultNavigator(startDestination = Destination.Splash)
    }

    @Provides
    @Singleton
    fun provideAlertDialogManager(): AlertDialogManager {
        return AlertDialogManager()
    }

    @Provides
    @Singleton
    fun provideScrollStateManager(): ScrollStateManager {
        return ScrollStateManager()
    }

    @Provides
    @Singleton
    fun providePackageManager(
        @ApplicationContext context: Context
    ): PackageManager {
        return context.packageManager
    }

    @Provides
    @Singleton
    fun provideWeb3AuthHelperImpl(web3Auth: Web3Auth): Web3AuthHelper {
        return Web3AuthHelperImpl(web3Auth)
    }

    @Provides
    @Singleton
    fun provideSolanaHelper(
        walletManager: WalletManager
    ): SolanaHelper {
        return SolanaHelperImpl(Connection(RpcUrl.DEVNET),walletManager)
    }

    @Provides
    @Singleton
    fun provideBiometricHelper(): BiometricHelper {
        return BiometricHelper()
    }
    
    @Provides
    @Singleton
    fun provideWalletManager(
        biometricHelper: BiometricHelper,
        activityHolder: ActivityHolder,
        @ApplicationContext context: Context
    ): WalletManager {
        return WalletManager(biometricHelper, activityHolder, context)
    }

    @Provides
    @Singleton
    fun provideNfcHelper(
        activityHolder: ActivityHolder
    ): NFCHelper {
        return NFCHelper(activityHolder.getActivity() )
    }
}