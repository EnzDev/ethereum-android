package ethereumjava.solidity.element.function;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import ethereumjava.Utils;
import ethereumjava.module.Eth;
import ethereumjava.module.objects.Block;
import ethereumjava.module.objects.BlockFilter;
import ethereumjava.module.objects.Hash;
import ethereumjava.module.objects.Transaction;
import ethereumjava.module.objects.TransactionRequest;
import ethereumjava.solidity.coder.SCoder;
import ethereumjava.solidity.element.SolidityElement;
import ethereumjava.solidity.element.returns.SingleReturn;
import ethereumjava.solidity.types.SArray;
import ethereumjava.solidity.types.SType;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by gunicolas on 4/08/16.
 */

public class SolidityFunction<T extends SType> extends SolidityElement{

    protected Object[] args;

    public SolidityFunction(String address, Method method, Eth eth, Object[] args) {
        super(address, method, eth);
        this.args = args;
    }

    @Override
    protected List<AbstractMap.SimpleEntry<Type,SArray.Size>> getParametersType() {
        Type[] parametersType = method.getGenericParameterTypes();
        Annotation[][] parametersAnnotations = method.getParameterAnnotations();

        List<AbstractMap.SimpleEntry<Type,SArray.Size>> ret = new ArrayList<>();
        for(int i=0;i<parametersType.length;i++){
            SArray.Size arraySize = Utils.arrayContainsAnnotation(parametersAnnotations[i], SArray.Size.class);
            ret.add(new AbstractMap.SimpleEntry<>(parametersType[i], arraySize));
        }
        return ret;
    }

    @Override
    protected String signature() {
        return super.signature().substring(0, 8);
    }

    private String encode() {
        String encodedParameters = "";
        if (args != null) {
            encodedParameters = SCoder.encodeParams(args);
        }
        return "0x" + this.signature() + encodedParameters;
    }

    private TransactionRequest formatRequest(String from, BigInteger gas, BigInteger value) {
        //TODO can estimate gas before
        String payload = encode();
        TransactionRequest request = new TransactionRequest(from, address);
        request.setGas(gas);
        request.setDataHex(payload);
        if (value != null) {
            request.setValueHex(value);
        }
        return request;
    }

    private TransactionRequest formatRequest(String from, BigInteger gas) {
        return formatRequest(from, gas, null);
    }

    public Hash sendTransaction(String from, BigInteger gas) {
        return eth.sendTransaction(formatRequest(from, gas));
    }

    public Hash sendTransaction(String from, BigInteger gas, BigInteger value) {
        return eth.sendTransaction(formatRequest(from, gas, value));
    }

    public Observable<Transaction> sendTransactionAndGetMined(String from, BigInteger gas) {

        final Hash txHash = sendTransaction(from, gas);
        if (txHash == null) {
            return null;
        }

        return new BlockFilter(eth)
            .watch()
            .flatMap(getBlockFromHash())
            .flatMap(getTransactionsOfBlock())
            .first(keepGivenTransaction(txHash));
    }

    private Func1<Transaction, Boolean> keepGivenTransaction(final Hash txHash) {
        return new Func1<Transaction, Boolean>() {
            @Override
            public Boolean call(Transaction transaction) {
                return transaction.hash.getValue().equals(txHash.getValue());
            }
        };
    }

    private Func1<Block<Transaction>, Observable<Transaction>> getTransactionsOfBlock() {
        return new Func1<Block<Transaction>, Observable<Transaction>>() {
            @Override
            public Observable<Transaction> call(Block<Transaction> transactionBlock) {
                return Observable.from(transactionBlock.transactions);
            }
        };
    }

    private Func1<String, Observable<Block<Transaction>>> getBlockFromHash() {
        return new Func1<String, Observable<Block<Transaction>>>() {
            @Override
            public Observable<Block<Transaction>> call(String blockHash) {
                return eth.getBlock(Hash.valueOf(blockHash), Transaction.class);
            }
        };
    }

    SType[] makeCallAndDecode(){
        String payload = encode();
        TransactionRequest request = new TransactionRequest(address);
        request.setDataHex(payload);

        String encodedResponse = eth.call(request, "latest");

        encodedResponse = encodedResponse.substring(2); // Remove 0x prefix

        if( returns.size() == 0 ) return null;

        return SCoder.decodeParams(encodedResponse,returns);
    }

    public SingleReturn<T> call() {
        SType[] decodedParams = makeCallAndDecode();
        return new SingleReturn(decodedParams[0]);
    }
}
