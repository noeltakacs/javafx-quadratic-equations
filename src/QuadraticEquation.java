public class QuadraticEquation { //Verejne prístupná trieda
    //Metóda preberá 3 parametre, z ktorých vypočíta kvadratickú rovnicu poďla daného vzorca
    public static float[] quadraticEquation(float coefA, float coefB, float coefC){ //Metóda musí vrátiť pole typu Float, lebo sa vypočíta výsledok pre x1 aj x2
        float discriminant = Discriminant.discriminant(coefA, coefB, coefC); //Najprv sa vypočíta diskriminant pomocou statickej metódy a priradí do lokálnej premennej
        
        if(coefA == 0){ //Podmienka, ktorá kontroluje, či je rovnica lineárna (kvôli tomu aby sa na výpočet použila metóda na počítanie lineárnej rovnice)
            float[] result = {LinearEquation.linearEquation(coefB, coefC)}; //do poľa priradí iba jednu hodnotu, t.j. výsledok lineárnej rovnice -> x
            return result; //Vráti výsledok
        }else if(discriminant == 0){ //Podmienka kontroluje, či je diskriminant 0 (či má iba jedno riešenie)
            float root = (float)(-coefB / (2 * coefA)); //do lokálnej premennej root(x) sa priradí výsledok kvadratickej rovnice

            float[] result = {root}; //Keďže rovnica má iba jedno riešenie, tak do poľa sa priradí iba jedna hodnota
            return result; //Vráti výsledok
        }else if(discriminant > 0){ ///Podmienka kontroluje, či je diskriminant väčší ako 0 (či má iba 2 riešenia)
            float root1 = (float)(-coefB + Math.sqrt(discriminant)) / (2 * coefA); //do lokálnej premennej root1(x1) sa priradí výsledok kvadratickej rovnice
            float root2 = (float)(-coefB - Math.sqrt(discriminant)) / (2 * coefA); //do lokálnej premennej root2(x2) sa priradí výsledok kvadratickej rovnice

            float [] result = {root1, root2}; //Keďže rovnica má 2 riešenia, tak do poľa sa priradia 2 hodnoty
            return result; //Vráti výsledok
        }

        return null; //Pokiaľ sa nevykonalo telo ani jednej podmienky, znamená to, že D < 0 (nemá riešenie) a vráti null
    }

    public static float findY(float coefA, float coefB, float coefC, float coordOfX){ //Metóda, ktorá pre daný bod X nájde bod Y podľa vzorca
        return (coefA * (float)Math.pow(coordOfX, 2) + coefB * coordOfX + coefC); //Vzorec: ax2 + bx + c
    }
}