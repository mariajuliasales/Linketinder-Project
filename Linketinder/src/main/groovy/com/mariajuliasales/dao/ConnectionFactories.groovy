package com.mariajuliasales.dao

final class ConnectionFactories {

	private static final Map<String, Closure<ConnectionFactory>> FACTORIES_BY_TYPE = [
		postgresql: { DatabaseConfig config -> new PostgresConnectionFactory(config) }
	]

	static ConnectionFactory forConfig(DatabaseConfig config) {
		Closure<ConnectionFactory> createFactory = FACTORIES_BY_TYPE[config.type?.toLowerCase()]
		if (!createFactory) {
			throw new IllegalArgumentException("Banco de dados não suportado: '${config.type}'. " +
			"Opções: ${supportedTypes().join(', ')}")
		}
		createFactory(config)
	}

	static Set<String> supportedTypes() {
		FACTORIES_BY_TYPE.keySet()
	}
}
