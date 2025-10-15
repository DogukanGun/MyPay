//
//  SolanaPayTransferCreator.swift
//  MyPayIos
//
//  Created by Dogukan Gundogan on 08.10.25.
//

import Foundation

// MARK: - Transaction Structure
struct SolanaTransaction {
    let instructions: [SolanaInstruction]
    let recentBlockhash: String
    let feePayer: String
    
    init(instructions: [SolanaInstruction], recentBlockhash: String, feePayer: String) {
        self.instructions = instructions
        self.recentBlockhash = recentBlockhash
        self.feePayer = feePayer
    }
}

// MARK: - Instruction Structure
struct SolanaInstruction {
    let programId: String
    let accounts: [SolanaAccountMeta]
    let data: Data
    
    init(programId: String, accounts: [SolanaAccountMeta], data: Data) {
        self.programId = programId
        self.accounts = accounts
        self.data = data
    }
}

// MARK: - Account Meta Structure
struct SolanaAccountMeta {
    let publicKey: String
    let isSigner: Bool
    let isWritable: Bool
    
    init(publicKey: String, isSigner: Bool, isWritable: Bool) {
        self.publicKey = publicKey
        self.isSigner = isSigner
        self.isWritable = isWritable
    }
}

// MARK: - Transfer Creator
struct SolanaPayTransferCreator {
    
    // System Program ID for SOL transfers
    private static let systemProgramId = "11111111111111111111111111111112"
    
    // Token Program ID for SPL token transfers
    private static let tokenProgramId = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
    
    // Associated Token Program ID
    private static let associatedTokenProgramId = "ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL"
    
    static func createTransfer(
        sender: String,
        fields: TransferRequestURLFields,
        recentBlockhash: String
    ) async throws -> SolanaTransaction {
        
        let instructions: [SolanaInstruction]
        
        if let splToken = fields.splToken {
            // Create SPL token transfer instruction
            instructions = try await createSPLTokenInstructions(
                sender: sender,
                recipient: fields.recipient,
                amount: fields.amount,
                splToken: splToken,
                tokenDecimal: fields.tokenDecimal
            )
        } else {
            // Create SOL transfer instruction
            let instructionData = try? createSOLTransferInstruction(
                sender: sender,
                recipient: fields.recipient,
                amount: fields.amount
            )
            guard let instructionData else {
                throw NSError(domain: "SolanaPayTransferCreator", code: 0, userInfo: [NSLocalizedDescriptionKey: "Transaction data is not correct"])
            }
            instructions = [instructionData]
        }
        
        return SolanaTransaction(
            instructions: instructions,
            recentBlockhash: recentBlockhash,
            feePayer: sender
        )
    }
    
    // MARK: - SOL Transfer
    
    private static func createSOLTransferInstruction(
        sender: String,
        recipient: String,
        amount: Decimal
    ) throws -> SolanaInstruction {
        
        // Convert amount to lamports (1 SOL = 1,000,000,000 lamports)
        let lamports = UInt64((amount * 1_000_000_000).formatted())
        guard let lamports else {
            throw NSError(domain: "SolanaPayTransferCreator", code: 0, userInfo: [NSLocalizedDescriptionKey: "Amount is not correct"])
        }
        // Create transfer instruction data
        var data = Data()
        data.append(contentsOf: [2, 0, 0, 0]) // Transfer instruction discriminator
        data.append(contentsOf: withUnsafeBytes(of: lamports.littleEndian) { Array($0) })
        
        let accounts = [
            SolanaAccountMeta(publicKey: sender, isSigner: true, isWritable: true),
            SolanaAccountMeta(publicKey: recipient, isSigner: false, isWritable: true)
        ]
        
        return SolanaInstruction(
            programId: systemProgramId,
            accounts: accounts,
            data: data
        )
    }
    
    // MARK: - SPL Token Transfer
    
    private static func createSPLTokenInstructions(
        sender: String,
        recipient: String,
        amount: Decimal,
        splToken: String,
        tokenDecimal: Int
    ) async throws -> [SolanaInstruction] {
        
        var instructions: [SolanaInstruction] = []
        
        // Derive associated token addresses
        let senderATA = try deriveAssociatedTokenAddress(owner: sender, mint: splToken)
        let recipientATA = try deriveAssociatedTokenAddress(owner: recipient, mint: splToken)
        
        // Check if recipient ATA exists (in a real implementation, you'd query the blockchain)
        // For now, we'll assume it needs to be created
        let createATAInstruction = createAssociatedTokenAccountInstruction(
            payer: sender,
            associatedToken: recipientATA,
            owner: recipient,
            mint: splToken
        )
        instructions.append(createATAInstruction)
        // Create SPL token transfer instruction
        let amount = (amount * Decimal(pow(10.0, Double(tokenDecimal)))).formatted()
        let transferAmount = UInt64(amount)
        guard let transferAmount = UInt64(amount) else {
            throw NSError(domain: "SolanaPayTransferCreator", code: 0, userInfo: [NSLocalizedDescriptionKey: "Amount is not correct"])
        }
        let transferInstruction = createSPLTransferInstruction(
            from: senderATA,
            to: recipientATA,
            owner: sender,
            amount: transferAmount
        )
        instructions.append(transferInstruction)
        
        return instructions
    }
    
    private static func createAssociatedTokenAccountInstruction(
        payer: String,
        associatedToken: String,
        owner: String,
        mint: String
    ) -> SolanaInstruction {
        
        let accounts = [
            SolanaAccountMeta(publicKey: payer, isSigner: true, isWritable: true),
            SolanaAccountMeta(publicKey: associatedToken, isSigner: false, isWritable: true),
            SolanaAccountMeta(publicKey: owner, isSigner: false, isWritable: false),
            SolanaAccountMeta(publicKey: mint, isSigner: false, isWritable: false),
            SolanaAccountMeta(publicKey: systemProgramId, isSigner: false, isWritable: false),
            SolanaAccountMeta(publicKey: tokenProgramId, isSigner: false, isWritable: false)
        ]
        
        return SolanaInstruction(
            programId: associatedTokenProgramId,
            accounts: accounts,
            data: Data() // No data needed for ATA creation
        )
    }
    
    private static func createSPLTransferInstruction(
        from: String,
        to: String,
        owner: String,
        amount: UInt64
    ) -> SolanaInstruction {
        
        // Create transfer instruction data
        var data = Data()
        data.append(contentsOf: [3]) // Transfer instruction discriminator
        data.append(contentsOf: withUnsafeBytes(of: amount.littleEndian) { Array($0) })
        
        let accounts = [
            SolanaAccountMeta(publicKey: from, isSigner: false, isWritable: true),
            SolanaAccountMeta(publicKey: to, isSigner: false, isWritable: true),
            SolanaAccountMeta(publicKey: owner, isSigner: true, isWritable: false)
        ]
        
        return SolanaInstruction(
            programId: tokenProgramId,
            accounts: accounts,
            data: data
        )
    }
    
    // MARK: - Helper Methods
    
    private static func deriveAssociatedTokenAddress(owner: String, mint: String) throws -> String {
        // This is a simplified version. In a real implementation, you'd use proper
        // program-derived address generation with the actual Solana SDK
        // For now, we'll return a placeholder that follows the pattern
        return "\(owner)_\(mint)_ata"
    }
}
