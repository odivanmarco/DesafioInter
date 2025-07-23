package fx.wallet.core.strategy.factory;

import fx.wallet.core.enums.TransferType;
import fx.wallet.core.strategy.RemittanceStrategy;
import fx.wallet.core.strategy.impl.BrlToBrlRemittanceStrategy;
import fx.wallet.core.strategy.impl.BrlToUsdRemittanceStrategy;
import fx.wallet.core.strategy.impl.UsdToBrlRemittanceStrategy;
import fx.wallet.core.strategy.impl.UsdToUsdRemittanceStrategy;
import jakarta.inject.Singleton;

import java.util.EnumMap;
import java.util.Map;

@Singleton
public class RemittanceFactory {
    private final Map<TransferType, RemittanceStrategy> strategyMap;

    public RemittanceFactory(BrlToBrlRemittanceStrategy brlToBrl,
                             BrlToUsdRemittanceStrategy brlToUsd,
                             UsdToBrlRemittanceStrategy usdToBrl,
                             UsdToUsdRemittanceStrategy usdToUsd) {
        strategyMap = new EnumMap<>(TransferType.class);
        strategyMap.put(TransferType.BRL_TO_BRL, brlToBrl);
        strategyMap.put(TransferType.BRL_TO_USD, brlToUsd);
        strategyMap.put(TransferType.USD_TO_BRL, usdToBrl);
        strategyMap.put(TransferType.USD_TO_USD, usdToUsd);
    }

    public RemittanceStrategy getStrategy(TransferType transferType) {
        return strategyMap.get(transferType);
    }
}
