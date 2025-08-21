package com.dag.mypayandroid.base.solanapay

import org.sol4k.Connection
import org.sol4k.Constants.ASSOCIATED_TOKEN_PROGRAM_ID
import org.sol4k.Constants.TOKEN_PROGRAM_ID
import org.sol4k.PublicKey
import org.sol4k.Transaction
import org.sol4k.instruction.CreateAssociatedTokenAccountInstruction
import org.sol4k.instruction.Instruction
import org.sol4k.instruction.SplTransferInstruction
import org.sol4k.instruction.TransferInstruction

object TransferCreator {
    fun createTransfer(
        connection: Connection,
        sender: PublicKey,
        fields: TransferRequestURLFields,
    ): Transaction {
        val instruction = if (fields.splToken != null) {
            createSPLTokenInstruction(
                connection,
                fields.recipient,
                fields.amount.toLong(),
                fields.splToken,
                sender,
                fields.tokenDecimal
            )
        } else {
            createTransferInstruction(sender,fields.recipient,fields.amount.toLong())
        }
        val blockHash = connection.getLatestBlockhash()
        val transaction = Transaction(recentBlockhash = blockHash, instructions = instruction, feePayer = sender)
        return transaction
    }

    private fun createTransferInstruction(
        sender: PublicKey,
        recipient: PublicKey,
        amount: Long
    ): List<Instruction> {
        return listOf(TransferInstruction(sender,recipient,amount))
    }

    private fun createSPLTokenInstruction(
        connection: Connection,
        recipient: PublicKey,
        amount: Long,
        splToken: PublicKey,
        sender: PublicKey,
        tokenDecimal: Int
    ): List<Instruction> {
        val (associatedAccount) = PublicKey.findProgramDerivedAddress(recipient, splToken)
        val accountInfo = connection.getAccountInfo(associatedAccount)
        val instructions = mutableListOf<Instruction>()
        val senderAta = deriveAssociatedTokenAddress(sender, splToken)
        // If ATA doesn’t exist, create it
        if (accountInfo == null) {
            instructions += CreateAssociatedTokenAccountInstruction(
                payer = sender,
                associatedToken = associatedAccount,
                owner = recipient,
                mint = splToken,
            )
        }
        // Add transfer instruction
        instructions += SplTransferInstruction(
            from = senderAta,
            to = associatedAccount,
            owner = sender,
            amount = amount,
            mint = splToken,
            signers = listOf(sender),
            decimals = tokenDecimal
        )
        return instructions
    }

    fun deriveAssociatedTokenAddress(owner: PublicKey, mint: PublicKey): PublicKey {
        val (address, _) = PublicKey.findProgramAddress(
            listOf(
                owner,
                TOKEN_PROGRAM_ID,
                mint
            ),
            ASSOCIATED_TOKEN_PROGRAM_ID
        )
        return address
    }
} 