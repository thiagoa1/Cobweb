package br.edu.uni7.ia.cobweb;

public class CreditProfile implements Sample {

	public static final String[] PROPERTIES = { "historicoDeCredito", "divida", "garantia", "renda" };

	// Classe
	public final String risco;
	// Propriedades
	public final String historicoDeCredito;
	public final String divida;
	public final String garantia;
	public final String renda;

	private final String[] stringData;

	public CreditProfile(String risco, String historicoDeCredito, String divida, String garantia, String renda) {
		this.risco = risco;
		this.historicoDeCredito = historicoDeCredito;
		this.divida = divida;
		this.garantia = garantia;
		this.renda = renda;

		stringData = new String[] { risco, historicoDeCredito, divida, garantia, renda };
	}

	public CreditProfile(String[] stringData) {
		if (stringData == null || stringData.length != 5) {
			throw new IllegalArgumentException("Argumento nulo ou fora do formato");
		}
		this.stringData = trimmedData(stringData);
		this.risco = stringData[0].trim();
		this.historicoDeCredito = stringData[1].trim();
		this.divida = stringData[2].trim();
		this.garantia = stringData[3].trim();
		this.renda = stringData[4].trim();
	}
	
	private String[] trimmedData(String[] stringData) {
		String[] trimmedData = new String[stringData.length];
		for (int i = 0; i < trimmedData.length; i++) {
			trimmedData[i] = stringData[i].trim();
		}
		return trimmedData;
	}

	@Override
	public String getClazz() {
		return risco;
	}

	@Override
	public String[] getProperties() {
		return PROPERTIES;
	}

	@Override
	public String getPropertyValue(String property) {
		int propertyIndex = 0;
		for (String propriedadeListada : PROPERTIES) {
			if (propriedadeListada.equals(property)) {
				break;
			}
			// TODO tratar erro quando propriedade não está no array
			propertyIndex++;
		}

		return stringData[propertyIndex + 1];
	}

	@Override
	public String toString() {
		return "[" + String.join(",", stringData) + "]";
	}
}
