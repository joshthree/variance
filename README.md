# Variance
This is my first research independent research project as part of my Master's thesis.  A few years later, with knowledge gained during my PhD studies, I refined the project.  The project is associated with a paper that was published in IEEE International Conference on Blockchain, where it won Best Paper Award.

This is also contains the beginnings of my ZKProver library, which can be used to construct logical compositions of zero knowledge proofs.

This library is the experimental framework model used to create the graphs and comparisons within the Variance paper.

## Project Description:

This project and paper are an attempt to solve Yao's Millionaire problem in the context of Bitcoin.  It consists of two major components: (1) The Proof of Assets, a process where a party adds up the value of your Bitcoin accounts in to a commitment of the value of the accounts owned without revealing which accounts are owned by the party and (2) comparing the commitment to another person's commitment.  

### Proof of Assets: 

### b

Known fundamental limitation:  While this can be used to prove that one party has more money than another party if the motivation is to prove who has more money, it can not be used to prove who has more money if it would be advantageous to a party to have less money.  You can not prove lack of knowledge, so a party could ignore their knowledge of a key associated with an account they own.

## To run:
This repository is in the form of an Eclipse workspace.  If you import the project, you can run 
