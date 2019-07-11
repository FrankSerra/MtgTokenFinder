package ThymeleafEntities;

public class TokenGuess {
	public String name, power, toughness;
	
	public TokenGuess(String _name, String _power, String _toughness) {
		this.name 	   = _name;
		this.power 	   = _power;
		this.toughness = _toughness;
	}

	//Overload equals for preparing array guesses
    public boolean equals(Object obj) {
        if (obj != null && getClass() == obj.getClass()) {
            TokenGuess q = (TokenGuess)obj;
            return name == q.name && power == q.power && toughness == q.toughness;
        }
        return false;
    }
}
