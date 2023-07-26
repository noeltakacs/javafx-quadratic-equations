public class Discriminant { //Verejne prístupná trieda
    public static float discriminant(float coefA, float coefB, float coefC){ //Metóda preberá 3 parametre, z ktorý vypočíta diskriminant podľa daného vzorca
        return (float)Math.pow(coefB, 2) - 4 * coefA * coefC; //Výpočet podľa vzorca: b2 - 4 * a * c
    }
}
