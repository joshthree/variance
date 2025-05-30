package protocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.io.TeeOutputStream;

import java.util.Base64.Decoder;

import zero_knowledge_proofs.ArraySizesDoNotMatchException;
import zero_knowledge_proofs.CheaterException;
import zero_knowledge_proofs.InvalidStringFormatException;
import zero_knowledge_proofs.MultipleTrueProofException;
import zero_knowledge_proofs.NoTrueProofException;
import zero_knowledge_proofs.VarianceToolkit;
import zero_knowledge_proofs.ECPedersenCommitment;
import zero_knowledge_proofs.ECPedersenOwnedBitwiseCommitment;
import zero_knowledge_proofs.ECSchnorrProver;
import zero_knowledge_proofs.ZKPProtocol;
import zero_knowledge_proofs.ZKToolkit;
import zero_knowledge_proofs.CryptoData.BigIntData;
import zero_knowledge_proofs.CryptoData.CryptoData;
import zero_knowledge_proofs.CryptoData.CryptoDataArray;
import zero_knowledge_proofs.CryptoData.ECCurveData;
import zero_knowledge_proofs.CryptoData.ECPointData;

public class ProtocolMainVarianceMultiSigMainParty {
	//TODO Make usage statement:  <excecutable> <ip> <port> <myport> <accounts file name> <key file name> <environment file name> <blockSize> <friends file> [seed (optional)]

	static boolean verify = true; //This is lousy, make it an instance
	static ECPedersenCommitment othersSumCommitment;
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException, InvalidStringFormatException, IOException, ClassNotFoundException, MultipleTrueProofException, NoTrueProofException, ArraySizesDoNotMatchException{
		int debug = 1;
		boolean logging = true;
		if(logging)
		{
			PrintStream err = new PrintStream(new File("outputs/"+args[4] + "_run_error"));
			PrintStream normal = new PrintStream(new File("outputs/" + args[4] + "_run_outputs"));
			err = new PrintStream(new TeeOutputStream(System.err, err));
			normal = new PrintStream(new TeeOutputStream(System.out, normal));
			System.setErr(err);
			System.setOut(normal);
		}
		Decoder decoder = Base64.getDecoder();
		System.out.println(new File("").getAbsolutePath());
		VarianceToolkit.register();
		ZKPProtocol ecSchnorr = new ECSchnorrProver();
		ZKPProtocol[][] provers = new ZKPProtocol[1][];
		CryptoData[][] environments = new CryptoData[1][];
		ServerSocket host = null;

		// Create and install a security manager
		System.out.println("Setting System Security Settings:");
		//System.setSecurityManager(new SecurityManager());
		System.setProperty("javax.net.ssl.trustStore", "resources/Client_Truststore");
		System.setProperty("javax.net.ssl.keyStore", "resources/Server_Keystore");
		System.setProperty("javax.net.ssl.trustStorePassword", "test123");
		System.setProperty("javax.net.ssl.keyStorePassword", "test123");
		System.setProperty("java.security.policy", "resources/mysecurity.policy");
		ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
		SocketFactory sf = SSLSocketFactory.getDefault();
		Socket s = null;
		ObjectInputStream mainIn = null;
		ObjectOutputStream mainOut = null;
		InetAddress mainAddress = InetAddress.getByName(args[0]);
		
		
		Scanner friendFile = new Scanner(new File("inputs/"+args[7]));
		ArrayList<InetAddress> friendIPs = new ArrayList<InetAddress>();
		while(friendFile.hasNextLine()) {
			String blah = friendFile.nextLine().trim();
			friendIPs.add(InetAddress.getByName(blah));
		}
		
		friendFile.close();
		SocketAddress dest = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
		
		Socket[] friends = new Socket[friendIPs.size()];	// TODO:  Use SSLSockets
		ObjectInputStream[] friendsIn = new ObjectInputStream[friendIPs.size()];
		ObjectOutputStream[] friendsOut = new ObjectOutputStream[friendIPs.size()];
		host = ssf.createServerSocket(Integer.parseInt(args[2]));
		int socketDelay =  (int)((2500 * Math.random()) + 2500);
		host.setSoTimeout(0);	//Shifted so that they are not in sync and will eventually connect.
		
		int connected = 0;
		
		boolean amHost = false;
		while(connected != friendsIn.length)
		{
			Socket temp;
			ObjectOutputStream tempOut;
			ObjectInputStream tempIn;
			try
			{
				System.out.println("Waiting for " + (1+friendsIn.length - connected) + " parties.");
				temp = host.accept();
				System.out.println("Someone contacted me!");
				temp.setSoTimeout(socketDelay);
				tempOut = new ObjectOutputStream(temp.getOutputStream());
				tempIn = new ObjectInputStream(temp.getInputStream());
				System.out.println("Streams open!");
			} catch (Exception e){
				System.out.println("Exception, trying again");
				continue;
			}
			int num;
			
			InetAddress address = temp.getInetAddress();
			try{
				System.out.println("Reading their int:");
				num = tempIn.readInt();
			}catch(IOException e) {
				System.out.println("odd...");
				try{
					temp.close();
				}catch(Exception e1) {}
				continue;
			}
			if(address.equals(mainAddress) && num == -1)
			{
				
				System.out.println("The other main party has connected!");
				if(s != null) {
					try {
						s.close();
					}catch(Exception e) {}
				}
				s = temp;
				mainOut = tempOut;
				mainIn = tempIn;
				host.setSoTimeout(0);
				System.out.println("Main Other Party Detected.");
				amHost = true;
			}
			else if(num < friendIPs.size()){
				if(friendIPs.get(num).equals(address) && friends[num] == null){
					friendsOut[num] = tempOut;
					friendsIn[num] = tempIn;
					friends[num] = temp; 
					tempOut.writeBoolean(true);
					tempOut.flush();
					System.out.println("Accepted friend!");
					connected++;
					
				}
			}
			else
			{
				try {
					System.out.println("I don't know this person...");
					tempOut.writeBoolean(false);
					tempOut.flush();
					temp.close();
				}catch(Exception e) {
					
				}
				continue;
			}
		}
		while(s == null)
		{
			host.setSoTimeout(socketDelay/5);
			try {
				s = host.accept();
				mainOut = new ObjectOutputStream(s.getOutputStream());
				mainIn = new ObjectInputStream(s.getInputStream());
				InetAddress address = s.getInetAddress();
				int num = mainIn.readInt();
				if(address.equals(mainAddress) && num == -1){
					System.out.println("Other main party has connected!");
					amHost = true;
					break;
				}else {
					System.out.println("Odd, I already have everyone else ready...");
					try{
						mainOut.writeBoolean(false);
						mainOut.flush();
						s.close();
					}catch(Exception e) {}
					s = null;
					continue;
				}
			}catch(Exception e) {
				if(s != null) {
					try {
						s.close();
					} catch (Exception e2) {}
					s = null;
				}
				try
				{
					s = sf.createSocket();
					s.setSoTimeout(socketDelay);
					s.connect(dest, 1000);
					
					System.out.println("I connected to the other's main party.");
					
					mainOut = new ObjectOutputStream(s.getOutputStream());
					mainIn = new ObjectInputStream(s.getInputStream());
					
					mainOut.writeInt(-1);
					mainOut.flush();
					
					host.setSoTimeout(0);
					connected++;
					
				} catch (SocketTimeoutException e2)
				{
					try {
						s.close();
					}catch(Exception e3) {}
					s = null;
					System.out.println("Trying again");
				}
			}
		}
		try {
			s.setSoTimeout(0);
			mainOut.writeBoolean(true);
			mainOut.flush();
			System.out.println("Sending ready signal");
			mainIn.readBoolean();
			System.out.println("Recieved ready signal");
		} catch (Exception e) {
			System.out.println("Ready Signal failed");
			try{
				s.close();
			}catch (Exception e2) {}
			s = null;
			connected--;
		}
		System.out.println(new Date());
		host.close();
		final long startTime = System.currentTimeMillis();
		BigInteger challengePrime = BigInteger.ONE.shiftLeft(255).subtract(BigInteger.valueOf(19)); //Closest prime to challenge space.
		InputStream fis = new FileInputStream("inputs/"+args[3]);
	    InputStreamReader isr = new InputStreamReader(fis);
	    BufferedReader br = new BufferedReader(isr);
	    
		InputStream keyFile = new FileInputStream("inputs/"+args[4]);
	    InputStreamReader isr2 = new InputStreamReader(keyFile);
	    BufferedReader keyBr = new BufferedReader(isr2);
	    
		InputStream envFile = new FileInputStream("inputs/"+args[5]);
	    InputStreamReader isr3 = new InputStreamReader(envFile);
	    BufferedReader envBr = new BufferedReader(isr3);

	    int blockSize = Integer.parseInt(args[6]);
	    int[] buffer = new int[16];
	    ECPedersenCommitment[] otherCommitments = new ECPedersenCommitment[blockSize];
		
	    System.out.println("Attempting to Connect");
	    SecureRandom r;
	    if(args.length == 9)
	    {
	    	BigInteger seedInt = new BigInteger(args[8]);
	    	byte[] seed = seedInt.toByteArray();
	    	r = new SecureRandom(seed);
	    }
	    else {
	    	r = new SecureRandom();
	    }
		
		
	    
		String dataRow;

		dataRow = envBr.readLine();
		String[] envString = dataRow.split("\t");
		ECPoint g = ECNamedCurveTable.getParameterSpec(envString[0]).getG();
		ECCurve curve = g.getCurve();
		BigInteger order = g.getCurve().getOrder();
		int bitLength = order.bitLength();

		BigInteger myPartOfKey = new BigInteger(bitLength, r);
		while(myPartOfKey.compareTo(order) >= 0)
		{
			myPartOfKey = new BigInteger(bitLength, r);
		}
		ECPoint halfH = g.multiply(myPartOfKey);
		mainOut.writeObject(halfH.getEncoded(true));
		mainOut.flush();
		ECPoint otherHalfH = curve.decodePoint((byte[]) mainIn.readObject());
		
		ECPoint h = halfH.add(otherHalfH);

		ArrayList<KeyList> knownKeys = new ArrayList<KeyList>();
		CryptoData miniEnv = new CryptoDataArray(new CryptoData[] {new ECCurveData(curve, g), new ECPointData(h)});
		
		CryptoData revMiniEnv = new CryptoDataArray(new CryptoData[] {new ECCurveData(curve, h), new ECPointData(g)});
		
		
		BigInteger sum = BigInteger.ZERO;
		BigInteger sumKey = BigInteger.ZERO;
		
		CryptoData environment = new CryptoDataArray(new CryptoData[] {new CryptoDataArray (new CryptoData[] {miniEnv, revMiniEnv}), revMiniEnv});

		othersSumCommitment = new ECPedersenCommitment(BigInteger.ZERO, BigInteger.ZERO, miniEnv);

		Writer arg0 = null;
		try {
			arg0 = new FileWriter("outputs/Verifier_Transcript_" + args[4] + "_Variance");
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		BufferedWriter out2 = new BufferedWriter(arg0);
		StringBuilder transcript = new StringBuilder();
		ArrayList<int[]> requests = new ArrayList<int[]>();
		while((dataRow = keyBr.readLine()) != null)
		{
			//String format:  "Public Key	Private Key"
			 
			String[] stringData = dataRow.split("\t");
			if(stringData[0].length() > 2 && stringData[0].substring(0, 2).equals("//")) {
				if(debug != 0)  System.out.printf("Key Line Omitted:  %s\n", dataRow);
				continue;	//lines can be commented with //
			}
			if(stringData.length < 3) 
			{
				System.err.printf("Line rejected from Keys:  %s\n", dataRow);
				continue;				
			}
			
			
			KeyList keys = new KeyList();
			
			try
			{
				keys.id = Integer.parseInt(stringData[0]);
				keys.keys = new CryptoData[stringData.length / 2];
				keys.positions = new int[stringData.length / 2];
				int internalReqIndex = 0;
				for(int i = 0; i < keys.keys.length; i++)
				{
					if(stringData[((i+1) * 2) - 1].charAt(0) != '-')
					{
						keys.positions[i] = Integer.parseInt(stringData[((i+1) * 2) - 1]);
						keys.keys[i] = new BigIntData(new BigInteger(decoder.decode(stringData[(i+1) * 2])));
					}
					else
					{
						keys.keys[i] = null;
						keys.positions[i] = -Integer.parseInt(stringData[((i+1) * 2) - 1]);

						buffer[internalReqIndex] = Integer.parseInt(stringData[(i+1) * 2]) - 1;
						internalReqIndex++;
					}
				}
				int[] request = null;
				if(internalReqIndex != 0){
					request = new int[internalReqIndex];
					for(int i = 0; i < internalReqIndex; i++)
					{
						request[i] = buffer[i];
					}
				}
				requests.add(request);
				knownKeys.add(keys);
			}
			catch(Exception e)
			{
				System.err.println("Line failed in KeyFile:  Not parsed as numbers.  " + dataRow);
				e.printStackTrace(System.err);
				continue;
			}
		}
		
		knownKeys.add(null);
		int index = 0;
		int proverIndex = 0;
		int counter = 0;
		CryptoData[] proverInput = new CryptoData[blockSize];
		CryptoData[] verifierInput = new CryptoData[blockSize];
		CryptoData[][] acc = new CryptoData[blockSize][];
		BigInteger balance[] = new BigInteger[blockSize];
		int lastN = -1;
		int lastK = -1;
		while((dataRow = br.readLine()) != null)
		{
			if(dataRow.length() == 0) continue;
			//String Format:  "Amount in account	Public Key" for each line. 
			String[] stringData = dataRow.split("\t");
			
			if(stringData[0].length() >= 2 && stringData[0].substring(0, 2).equals("//")) {
				if(debug != 0)  System.out.printf("Account Line Omitted:  %s\n", dataRow);
				continue;	//lines can be commented with //
			}
			if(stringData.length < 4) 
			{
				System.err.printf("Line rejected from accounts:  %s\n", dataRow);
				continue;				
			}
			int n = 0;
			int k = 0;
//			if(counter % 10 == 0) System.out.println(counter + " " + new Date());
//			if(counter % 2<<15 == 0)
//			{
			mainOut.flush();
			mainOut.reset();
//			}
			try
			{
				n = Integer.parseInt(stringData[1]);
				k = Integer.parseInt(stringData[2]);
				acc[proverIndex] =  new CryptoData[n];
				balance[proverIndex] = new BigInteger(stringData[0], 16);
				for(int i = 0; i < n; i++)
				{
					acc[proverIndex][i] = new ECPointData(curve.decodePoint(decoder.decode(stringData[i + 3])));
				}
			}
			catch(Exception e)
			{
				System.err.println("Line failed in AccountFile:  Not parsed correctly.  " + dataRow);
				continue;
			}
			KeyList keys = knownKeys.get(index);
			
			BigInteger random;
			ECPedersenCommitment commitment;
			if(provers.length < n)
			{
				ZKPProtocol[][] temp = new ZKPProtocol[n][];
				CryptoData[][] temp2 = new CryptoData[n][];
				for(int i = 0; i < provers.length; i++)
				{
					temp[i] = provers[i];
					temp2[i] = environments[i];
				}
				provers = temp;
				environments = temp2;
				provers[n-1] = new ZKPProtocol[n];
				environments[n-1] = new CryptoData[n];
				
				ZKPProtocol keyProtocol = VarianceToolkit.createMultiSigProofThreshhold(n, k, ecSchnorr,challengePrime);

				provers[n-1][k-1] = VarianceToolkit.createVarianceMultiSigProof(keyProtocol, ecSchnorr);
				
				CryptoData keyEnv = VarianceToolkit.createMultiSigEnvironmentThreshhold(n, k, miniEnv);
				environments[n-1][k-1] = VarianceToolkit.createVarianceEnvironment(keyEnv, miniEnv);
//				System.out.println(keyEnv);
			}
			else if(provers[n-1] == null)
			{
				provers[n-1] = new ZKPProtocol[n];
				environments[n-1] = new CryptoData[n];

				ZKPProtocol keyProtocol = VarianceToolkit.createMultiSigProofThreshhold(n, k, ecSchnorr, challengePrime);

				provers[n-1][k-1] = VarianceToolkit.createVarianceMultiSigProof(keyProtocol, ecSchnorr);
				
				CryptoData keyEnv = VarianceToolkit.createMultiSigEnvironmentThreshhold(n, k, miniEnv);
				environments[n-1][k-1] = VarianceToolkit.createVarianceEnvironment(keyEnv, miniEnv);
//				System.out.println(keyEnv);
			
			}
			else if(provers[n-1][k-1] == null)
			{
				ZKPProtocol keyProtocol = VarianceToolkit.createMultiSigProofThreshhold(n, k, ecSchnorr, challengePrime);

				provers[n-1][k-1] = VarianceToolkit.createVarianceMultiSigProof(keyProtocol, ecSchnorr);
				
				CryptoData keyEnv = VarianceToolkit.createMultiSigEnvironmentThreshhold(n, k, miniEnv);
				environments[n-1][k-1] = VarianceToolkit.createVarianceEnvironment(keyEnv, miniEnv);
//				System.out.println(keyEnv);
			}
			if(lastN != n || lastK != k){
				transcript.append("Environment:  ");
				transcript.append(environments[n-1][k-1].toString64());
				transcript.append("\n\n");
				lastN = n;
				lastK = k;
			}
//			System.out.println(provers[n-1][k-1]);
//			System.out.println("In main:  " + environments[n-1][k-1]);
			BigInteger pcKey;
			do{
				pcKey = new BigInteger(bitLength, r);
			}while (pcKey.compareTo(order) >= 0);
			
			int[] request = null;
			if(keys != null && keys.id == counter && keys.keys.length >= k){ 
				request = requests.get(index);
				index++;

				commitment = new ECPedersenCommitment(balance[proverIndex], pcKey, miniEnv);
				ECPoint comm = commitment.getCommitment(miniEnv);
				CryptoData keyData = VarianceToolkit.createMultiSigProverDataThreshhold(n, k, acc[proverIndex], keys.keys, keys.positions, miniEnv, ecSchnorr, order, r);
				proverInput[proverIndex] = VarianceToolkit.createVarianceProverData(keyData, pcKey, comm, balance[proverIndex], miniEnv, true, r);
				sumKey = sumKey.add(pcKey).mod(order);
				sum = sum.add(balance[proverIndex]);
			}
			else
			{
				sumKey = sumKey.add(pcKey).mod(order);
				
				commitment = new ECPedersenCommitment(BigInteger.ZERO, pcKey, miniEnv);
				ECPoint comm = commitment.getCommitment(miniEnv);
				CryptoData keyData = VarianceToolkit.createMultiSigSimulatorDataThreshhold(n, k, acc[proverIndex], ecSchnorr, order, r, challengePrime);
				proverInput[proverIndex] = VarianceToolkit.createVarianceProverData(keyData, pcKey, comm, balance[proverIndex], miniEnv, false, r);
			}
			mainOut.writeObject(commitment);
			mainOut.flush();
			counter++;
			proverIndex++;
			if((proverIndex == blockSize || (!br.ready() && proverIndex != 0)))
			{
				for(int i = 0; i < proverIndex; i++)
				{
					try {
						otherCommitments[i] = (ECPedersenCommitment) mainIn.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
				mainOut.flush();
				for(int i = 0; i < proverIndex; i++)
				{
					ECPoint comm = otherCommitments[i].getCommitment(miniEnv);					
					CryptoData keyVerifierData = VarianceToolkit.createMultiSigVerifierInputsThreshhold(n, k, acc[i], ecSchnorr, challengePrime);
					verifierInput[i] = VarianceToolkit.createVarianceVerifierData(keyVerifierData, comm, balance[i], miniEnv);
					
					try {

						random = new BigInteger(bitLength, r);
						while(random.compareTo(order) >= 0)
							random = new BigInteger(bitLength, r);
						BigInteger[] c = new BigInteger[] {new BigInteger(bitLength - 1, r),random};
						ECPedersenCommitment myCmt = new ECPedersenCommitment(c[0], c[1], miniEnv);
						
						if(request == null) {
							verify = provers[n-1][k-1].parallelZKProve(proverInput[i], verifierInput[i], environments[n-1][k-1], mainIn, mainOut, myCmt, miniEnv, c, transcript);
						}
						else {
							verify = provers[n-1][k-1].parallelZKProveWithFriends(proverInput[i], verifierInput[i], environments[n-1][k-1], mainIn, mainOut, myCmt, miniEnv, c, friendsOut, friendsIn, request, transcript);
						}
						if(!(verify)) {
							System.out.println("Proof " + (counter) + ":  Proof was bad");
							System.out.println("proverInputs: " + proverInput[i]);;
							System.out.println("verifierInputs: " + verifierInput[i]);
//									System.out.println(myVerify + " " + otherVerify);
							break;
						}
						else
						{
							othersSumCommitment = othersSumCommitment.multiplyCommitment(otherCommitments[i], miniEnv);
						}
							
					} catch (ClassNotFoundException | MultipleTrueProofException | NoTrueProofException
							| ArraySizesDoNotMatchException e) {
						
						e.printStackTrace();
					}
				}
				out2.write(transcript.toString());
				transcript.setLength(0);
				mainOut.flush();
				proverIndex = 0;
			}
		}
		if(proverIndex != 0)
		{
			System.out.println("Argh...");
		}


		envBr.close();
		keyBr.close();
		br.close();

		if(verify)
			System.out.println("Success!  I believe the proof of assets");
		else 
		{
			System.out.println("FAIL");
		}

		mainOut.flush();
		final long endTime = System.currentTimeMillis();
		System.out.println("I have " + sum);
//		Debugging stuff:  Opens the commitment to see if the protocol executed correctly
//		out.writeObject(sum);
//		out.writeObject(sumKey);
//		BigInteger otherSum = (BigInteger) in.readObject();
//		BigInteger otherKeySum = (BigInteger) in.readObject();
		//Convert to Bits:

			
		ECPedersenOwnedBitwiseCommitment ecBits = null;
		int numBits = 51;
		ecBits = VarianceToolkit.ecConvertToBits(sum, sumKey, numBits, miniEnv, r);

		ECPedersenCommitment[] otherECBits = null;

		ECPedersenCommitment[] ecBitComm = ecBits.getComm();
		BigInteger[] ecBitKeys = ecBits.getKeys();
		
		mainOut.writeObject(ecBitComm);
		otherECBits = (ECPedersenCommitment[]) mainIn.readObject();
		
		if(!VarianceToolkit.checkBitCommitment(othersSumCommitment, otherECBits, miniEnv)) System.out.println("LIAR!!! BIT COMMITMENTS DO NOT MATCH THEIR SUM");
		else System.out.println("Bit Commitments Correct");
		System.out.println(ecBits.getMessage());
		CryptoData[][] table = VarianceToolkit.getBasicTable(miniEnv);
		CryptoData[][] newTable;
		
		BigInteger[] keys = new BigInteger[4];
		CryptoData tableProofEnv = VarianceToolkit.getTableProofEnvironment(miniEnv);
		CryptoData shuffleProofEnv = VarianceToolkit.getShuffleProofEnvironment(miniEnv);

		for(int j = 0; j < 4; j++)
		{
			keys[j] = new BigInteger(bitLength, r);
			while(keys[j].compareTo(order) >= 0)
				keys[j] = new BigInteger(bitLength, r);
		}
		CryptoData[] encryptions = VarianceToolkit.createTableCommitments(table[1], keys, ecBits.getMessage().testBit(numBits-1 - 0), !amHost, miniEnv);
		CryptoData pInputs = VarianceToolkit.getTableCoorespondenceProverData(table[1], encryptions, keys, new ECPointData(ecBitComm[numBits-1 - 0].getCommitment(miniEnv)), ecBitKeys[numBits-1 - 0], ecBits.getMessage().testBit(numBits-1 - 0), amHost, miniEnv, r);
		mainOut.writeObject(encryptions);
		CryptoData[] otherEncryptions = (CryptoData[]) mainIn.readObject();
		CryptoData vInputs = VarianceToolkit.getTableCoorespondenceVerifierData(table[1], otherEncryptions, new ECPointData(otherECBits[numBits-1 - 0].getCommitment(miniEnv)), !amHost, miniEnv);

		BigInteger random;
		random = new BigInteger(bitLength, r);
		while(random.compareTo(order) >= 0)
		{
			random = new BigInteger(bitLength, r);
		}
		BigInteger[] c = {new BigInteger(bitLength - 1, r), random};
		ECPedersenCommitment myCmt = new ECPedersenCommitment(c[0], c[1], miniEnv);
		ObjectInputStream[] inArray = {mainIn}; 
		ObjectOutputStream[] outArray = {mainOut}; 
		if(VarianceToolkit.consistantTableEncryptionProver.parallelZKProve(pInputs, vInputs, tableProofEnv, mainIn, mainOut, myCmt, miniEnv, c, null))
		{
		}
		else System.out.println("boooooo...");
		ECPoint[] feedback = new ECPoint[2];
		int[] shuffle = new int[3];
		BigInteger[][] keyChanges = new BigInteger[3][5];
		
		ECPoint inf = curve.getInfinity();

		int row = 1;
		for(int i = 1; i < numBits; i++)
		{
			feedback[0] = inf;
			feedback[1] = inf;
			for(int j = 0; j < 4; j++)
			{
				CryptoData[] temp = encryptions[j].getCryptoDataArray();
				feedback[0] = feedback[0].add(temp[0].getECPointData(curve));
				feedback[1] = feedback[1].add(temp[1].getECPointData(curve));
				
				temp = otherEncryptions[j].getCryptoDataArray();
				feedback[0] = feedback[0].add(temp[0].getECPointData(curve));
				feedback[1] = feedback[1].add(temp[1].getECPointData(curve));
			}
			for(int j = 0; j < 3; j++)
			{
				for(int k = 0; k < 5; k++)
				{
					keyChanges[j][k] = new BigInteger(bitLength, r);
					while(keyChanges[j][k].compareTo(order) >= 0)
						keyChanges[j][k] = new BigInteger(bitLength, r);
				}
			}
			shuffle[0] = shuffle[1] = shuffle[2] = 0;
			shuffle[r.nextInt(3)] = 2;
			int pos = r.nextInt(2);
			if(shuffle[pos] != 0)
				shuffle[2] = 1;
			else
				shuffle[pos] = 1;
			CryptoData[][] finalTable;
			CryptoData shufflePInputs;
			CryptoData shuffleVInputs;
			
			
			if(amHost)
			{
				newTable = VarianceToolkit.shuffleTable(table, keyChanges, shuffle, miniEnv);
				mainOut.writeObject(newTable);
				shufflePInputs = VarianceToolkit.createZeroKnowledgeProverInputsForShuffle(table, newTable, keyChanges, shuffle, miniEnv, r);
				finalTable = (CryptoData[][]) mainIn.readObject();
				
				shuffleVInputs = VarianceToolkit.createZeroKnowledgeVerifierInputsForShuffle(newTable, finalTable, miniEnv);
			}
			else
			{
				newTable = (CryptoData[][]) mainIn.readObject();
				finalTable = VarianceToolkit.shuffleTable(newTable, keyChanges, shuffle, miniEnv);
				mainOut.writeObject(finalTable);
				shufflePInputs = VarianceToolkit.createZeroKnowledgeProverInputsForShuffle(newTable, finalTable, keyChanges, shuffle, miniEnv, r);
				
				shuffleVInputs = VarianceToolkit.createZeroKnowledgeVerifierInputsForShuffle(table, newTable, miniEnv);
			}
			random = new BigInteger(bitLength, r);
			while(random.compareTo(order) >= 0)
			{
				random = new BigInteger(bitLength, r);
			}
			c = new BigInteger[2];
			c[0] = new BigInteger(bitLength - 1, r);
			c[1] = random;
			myCmt = new ECPedersenCommitment(c[0], c[1], miniEnv);
			
			if(!VarianceToolkit.tableEqualityProver.parallelZKProve(shufflePInputs, shuffleVInputs, shuffleProofEnv, mainIn, mainOut, myCmt, miniEnv, c, null))
			{
				System.out.println("CHEATING SHUFFLE");
			}
			for(int j = 0; j < 4; j++)
			{
				keys[j] = new BigInteger(bitLength, r);
				while(keys[j].compareTo(order) >= 0)
					keys[j] = new BigInteger(bitLength, r);
			}
			encryptions = VarianceToolkit.createTableCommitments(table[row], keys, ecBits.getMessage().testBit(numBits-1 - i), !amHost, miniEnv);
			pInputs = VarianceToolkit.getTableCoorespondenceProverData(table[row], encryptions, keys, new ECPointData(ecBitComm[numBits-1 - i].getCommitment(miniEnv)), ecBitKeys[numBits-1 - i], ecBits.getMessage().testBit(numBits-1 - i), amHost, miniEnv, r);
			mainOut.writeObject(encryptions);
			otherEncryptions = (CryptoData[]) mainIn.readObject();
			vInputs = VarianceToolkit.getTableCoorespondenceVerifierData(table[row], otherEncryptions, new ECPointData(otherECBits[numBits-1 - i].getCommitment(miniEnv)), !amHost, miniEnv);
			random = new BigInteger(bitLength, r);
			while(random.compareTo(order) >= 0)
			{
				random = new BigInteger(bitLength, r);
			}

			c = new BigInteger[2];
			c[0] = new BigInteger(bitLength - 1, r);
			c[1] = random;
			myCmt = new ECPedersenCommitment(c[0], c[1], miniEnv);
			VarianceToolkit.consistantTableEncryptionProver.parallelZKProve(pInputs, vInputs, tableProofEnv, mainIn, mainOut, myCmt, miniEnv, c, null);
			//tables are shuffled and proven to be equal!  Now, PET.
			for(row = 0;row < 3;row++)
			{
				int x = 0;
				if(!amHost)
				{
					x = 1;
				}
				try {
					if(ZKToolkit.plaintextEqualityTest(table[row][0].getCryptoDataArray(), new CryptoDataArray(feedback).getCryptoDataArray(), myPartOfKey, inArray, outArray, miniEnv, -1 + x, 0 - x, r))
					{
						break;
					}
					
					
				} catch (CheaterException e) {
					System.out.println("CHEATER");
					e.printStackTrace();
					System.exit(0);
				}
			}
			if(row == 3) System.out.println("ALL IS LOST!!  THE END IS NEAR!!!");
			for(int j = 0; j < 4; j++)
			{
				keys[j] = new BigInteger(bitLength, r);
				while(keys[j].compareTo(order) >= 0)
					keys[j] = new BigInteger(bitLength, r);
			}
			
		}
		//Decrypt feedback
		int resultOfComparison;
		if(!amHost)
		{
			feedback[0] = ZKToolkit.decryptECElgamal(new CryptoDataArray(feedback), myPartOfKey, miniEnv);
			mainOut.writeObject(new CryptoDataArray(feedback));
			resultOfComparison = (int) mainIn.readObject();
		}
		else
		{
			feedback[0] = ZKToolkit.decryptECElgamal((CryptoData) mainIn.readObject(), myPartOfKey, miniEnv);
			if(feedback[0].equals(inf)) resultOfComparison = 0;
			else if(feedback[0].equals(g)) resultOfComparison = -1;
			else resultOfComparison = 1;
			mainOut.writeObject(-resultOfComparison);
		}
		System.out.println(resultOfComparison);
				
//		if(verified) System.out.println("Good bit commitment");
//		else 
//		{
//			System.out.println("LIAR BAD BIT COMMITMENT");
//			System.out.println(transcript.toString());
//		}
		
		mainOut.flush();
		final long actualEnd = System.currentTimeMillis();
		System.out.println(counter);
		System.out.println("My sum is " + sum);
		if(resultOfComparison == 1) System.out.println("I have MORE HAHAHAHA");
		if(resultOfComparison == 0) System.out.println("We have equal amounts");
		if(resultOfComparison == -1) System.out.println("I have less :-(");
		System.out.println("Comparison runtime:  " + (actualEnd - endTime));
		

		System.out.println("Total execution time: " + (endTime - startTime) );
		try {
			arg0 = new FileWriter("outputs/Output_" + args[4]);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		BufferedWriter out1 = new BufferedWriter(arg0);

		if(verify) {
			out1.write("Success!\n");
			out2.write("Success!");
		}
		else{
			out1.write("FAILURE");
			out2.write("FAILURE");
		}
		for(int i = 0; i < friends.length; i++)
		{
			try {
				friendsOut[i].writeBoolean(true);
				friendsOut[i].flush();
				friends[i].setSoTimeout(1000);
				friendsIn[i].readBoolean();
			}catch(Exception e) {}
			friends[i].close();
			
		}
		out1.write(String.format("Total execution time: %d\n", (endTime - startTime)));
		out1.flush();
		s.setSoTimeout(1000);
		try {
			mainOut.writeBoolean(true);
			mainOut.flush();
			mainIn.readBoolean();
		}catch(Exception e) {}
		mainOut.close();
		out1.close();
		out2.flush();
		out2.close();
		mainIn.close();
		s.close();
		if(host != null) host.close();
		System.exit(0);
	}
	
}
