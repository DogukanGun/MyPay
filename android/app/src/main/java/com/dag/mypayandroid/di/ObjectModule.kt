package com.dag.mypayandroid.di

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import com.dag.mypayandroid.base.helper.AlertDialogManager
import com.dag.mypayandroid.base.helper.SolanaHelper
import com.dag.mypayandroid.base.helper.SolanaHelperImpl
import com.dag.mypayandroid.base.helper.Web3AuthHelper
import com.dag.mypayandroid.base.helper.Web3AuthHelperImpl
import com.dag.mypayandroid.base.navigation.DefaultNavigator
import com.dag.mypayandroid.base.navigation.Destination
import com.dag.mypayandroid.base.scroll.ScrollStateManager
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.Network
import com.web3auth.core.types.Web3AuthOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.sol4k.Connection
import javax.inject.Singleton

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
    fun provideWeb3Auth(@ApplicationContext context: Context): Web3Auth{
        return Web3Auth(
            Web3AuthOptions(
                clientId = "BPi5PB_UiIZ-cPz1GtV5i1I2iOSOHuimiXBI0e-Oe_u6X3oVAbCiAZOTEBtTXw4tsluTITPqA8zMsfxIKMjiqNQ",
                network = Network.SAPPHIRE_MAINNET,
                redirectUrl = Uri.parse( "com.dag.mypayandroid://auth")
            ), context
        )
    }

    @Provides
    @Singleton
    fun provideWeb3AuthHelperImpl(web3Auth: Web3Auth): Web3AuthHelper {
        return Web3AuthHelperImpl(web3Auth)
    }

    @Provides
    @Singleton
    fun provideSolanaHelper(): SolanaHelper {
        return SolanaHelperImpl(Connection(""))
    }
}