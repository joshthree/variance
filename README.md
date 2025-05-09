# Variance
This is my first research independent research project as part of my Master's thesis.  A few years later, with knowledge gained during my PhD studies, I refined the project.  The project is associated with a paper that was published in IEEE International Conference on Blockchain, where it won Best Paper Award.

This also contains the beginnings of my ZKProver library, which can be used to construct logical compositions of zero knowledge proofs.

This library is the experimental framework model used to create the graphs and comparisons within the Variance paper.  It creates a fake ledger equivalent to something that could be generated from the Bitcoin ledger, reducing the accounts down to a public key and an account size.  The protocol then runs on that list.  This gives the user the flexibility to test the protocol on a variety of configurations without having to interact with the blockchain or testnet, whose behavior is well known.

## Usage

This repository is in the form of an Eclipse workspace.  If you import the project in to Eclipse, all the dependencies are present.  Command line arguments are listed in a comment at the top of the files in the package "protocol."

Note that these are internal implementations designed to assess performance of the protocol.

This README is a work in progress as of 2025-05-08.

## Project Description:

This project and paper are an attempt to solve Yao's Millionaire problem in the context of Bitcoin.  It consists of two major components: (1) The Proof of Assets, a process where a party adds up the value of your Bitcoin accounts in to a commitment of the value of the accounts owned without revealing which accounts are owned by the party and (2) comparing the commitment to another person's commitment.  

### Proof of Assets: 

A Proof of Assets in Bitcoin is a proof that a commitment or ciphertext contains a sum of assets owned by a party.  A major goal of this project was to do accomplish this Proof of Assets without revealing which accounts are owned by the party performing the proof.  This is not the first time doing this, as the paper Provisions did the same thing.  Like Provisions, we accomplish our goal using Zero Knowledge Proofs, proving that a homomorphically additive commitment hides the same value of an account only if the party owns the account.  However, where the two papers differ is that Provisions used a tailor-made proof to accomplish this, whereas Variance used a more modular design for the Zero Knowledge Proof utilizing logical composition of zero knowledge proofs:

1.  Zero Knowledge AND, where out of the *n* statements involved in the proof, all *n* must be true.
2.  Zero Knowledge OR, where out of the *n* statements involved in the proof, only 1 must be true.
3.  Zero Knowledge Polynomial Threshold, where out of *n* statements, *k* statements must be true (where *k* is defined as a known proof parameter defined by the protocol)

Using this, I was able to replicate the effects of the Proof of Assets presented in Provisions with a 23% faster execution time by proving the following statement:
    "Either I know the private key account *i* AND my commitment hides the value in the Bitcoin address *i* OR my commitment hides 0."

Later, I generalized this to the following:
    "Either I own account *i* AND my commitment hides the value in the Bitcoin address *i* OR my commitment hides 0."

This allowed me to use a more generalized Proof of Ownership, allowing me to cover multiple signature accounts without having to do any major rearrangement of the proof.  For a single-key account, it turns out to be identical, utilizing Schnorr's protocol to prove ownership of a key.  For multi-sig, the Prover uses Polynomial Threshold to prove knowledge of *k* out of *n* keys.  This ability to include multi-sig accounts is a new feature of this protocol, as the proof in Provisions could only handle single-key accounts without a major overhaul to the design of the proof.

As the list of Bitcoin accounts is processed, the commitments are homomorphically added to produce a sum of the total assets upon completion of the last Bitcoin account on the list.

### Comparison:

To execute the comparison, the parties convert their total asset commitments to bit-wise commitments, which are commitments of either 0 or 1 that represent the bitwise representation of the total assets commitment plaintext.  

Then, we use a variant of the Mix and Match protocol to execute a bit-wise comparison between the two bit-wise commitments.  The parties create a table that contains two layers:  The first is to choose which logic gate to use for the comparison, then the second is to compare the bits.  So the current state of the comparison (less than, equal to, or greater than) is used to determine which logic gate to go with.  Then, the current bits from both commitments are used to determine the next state of the comparison.  The table is shuffled and rerandomized so that neither party knows which output is associated with which input.

### Limitations

Known fundamental limitation:  While this protocol can be used to prove that one party has more money than another party if the motivation is to prove who has more money, it can not be used to prove who has more money if it would be advantageous to a party to have less money.  You can not prove lack of knowledge, so a party could ignore their knowledge of a key associated with an account they own.  

Additionally, a party could cheat by colluding with another party who owns Bitcoin.  Their collusion partner could input the parts of the proof that are required to include their assets in the proof without revealing their keys, allowing a party to inflate their assets in a way that is indistinguishable from them actually knowing the material.

A little musing:  Ironically, Monero, a more privacy-focused cryptocurrency, could have a similar protocol that theoretically requires you to prove that a Monero account is not associated with a Monero address, meaning you could prove something related to non-ownership.  This could be used by law enforcement to determine how much money a person has in Monero.  However, it is still not foolproof, even if it is run on the entire blockchain (which would be prohibitively expensive).  A Monero user could have more than one address, and can therefore omit some of their addresses, thereby hiding their assets.  Monero's Proof of Ownership would also require a proof that the account has not been spent, which would require a proof on the key images associated with transactions that included the given account as a ring member.  It would also require a proof of the Hash to Point function used by Monero, which is not included in this repository.

