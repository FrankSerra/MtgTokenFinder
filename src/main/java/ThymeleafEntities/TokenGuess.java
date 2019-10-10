package ThymeleafEntities;

import java.util.Objects;

public class TokenGuess {
	public final String name;
    public final String power;
    public final String toughness;
	
	public TokenGuess(String _name, String _power, String _toughness) {
		this.name 	   = _name;
		this.power 	   = _power;
		this.toughness = _toughness;
	}

	//Overload equals for preparing array guesses
    public boolean equals(Object obj) {
        if (obj != null && getClass() == obj.getClass()) {
            TokenGuess q = (TokenGuess)obj;
            return Objects.equals(name, q.name) && Objects.equals(power, q.power) && Objects.equals(toughness, q.toughness);
        }
        return false;
    }
}
