public class LinearEquation { //Verejne prístupná trieda
    public static float linearEquation(float coefA, float coefB){ //Metóda, ktorá preberá 2 parametre a následne vypočíta lineárnu rovnicu podľa vzorca
        return -(coefB / coefA); //Upravený vzorec ax + b = 0 -> x = -(b/a)
    }

    public static float findY(float coefA, float coefB, float coordOfX){ //Metóda, ktorá pre daný bod X nájde bod Y podľa vzorca
        return coefA * coordOfX + coefB; //Vzorec: ax + b
    }
}
