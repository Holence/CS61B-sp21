package gh2;

import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;
import deque.ArrayDeque;

public class GuitarHero {

    public static void main(String[] args) {
        String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
        ArrayDeque<GuitarString> strings = new ArrayDeque<>();
        for (int i = 0; i < keyboard.length(); i++) {
            GuitarString t = new GuitarString(440 * Math.pow(2, (i - 24) / 12.0));
            strings.addLast(t);
        }

        int index;
        char key;
        while (true) {

            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                key = StdDraw.nextKeyTyped();
                index = keyboard.indexOf(key);
                System.out.println(key);
                if (index == -1) {
                    continue;
                }
                strings.get(index).pluck();
            }

            /* compute the superposition of samples */
            double sample = 0;
            for (GuitarString s : strings) {
                sample += s.sample();
            }

            /* play the sample on standard audio */
            StdAudio.play(sample);

            /* advance the simulation of each guitar string by one step */
            for (GuitarString s : strings) {
                s.tic();
            }
        }
    }
}
