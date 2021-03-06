package ethereumjava.solidity.element.function;

import java.lang.reflect.Method;

import ethereumjava.module.Eth;
import ethereumjava.solidity.element.returns.TripleReturn;
import ethereumjava.solidity.types.SType;

/**
 * Created by gunicolas on 22/03/17.
 */

public class SolidityFunction3<T1 extends SType, T2 extends SType,T3 extends SType> extends SolidityFunction2 {

    public SolidityFunction3(String address, Method method, Eth eth, Object[] args) {
        super(address, method, eth, args);
    }

    @Override
    public TripleReturn<T1,T2,T3> call() {
        SType[] decodedParams = makeCallAndDecode();
        return new TripleReturn(decodedParams[0],decodedParams[1],decodedParams[2]);
    }
}
