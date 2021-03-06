package ethereumjava.solidity.coder.decoder.suintdecoder;

import ethereumjava.solidity.SolidityUtils;
import ethereumjava.solidity.coder.decoder.SDecoder;
import ethereumjava.solidity.types.SInt;
import ethereumjava.solidity.types.SUInt;

/**
 * Created by gunicolas on 17/03/17.
 */

public class SUInt64Decoder implements SDecoder<SUInt.SUInt64> {

    @Override
    public SUInt.SUInt64 decode(String toDecode) {
        if( !toDecode.toLowerCase().startsWith("0x") ){
            toDecode = "0x"+toDecode;
        }
        return SUInt.fromBigInteger64(SolidityUtils.hexToBigDecimal(toDecode).toBigInteger());
    }
}
