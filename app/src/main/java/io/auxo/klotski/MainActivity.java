package io.auxo.klotski;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import io.auxo.klotski.model.Block;
import io.auxo.klotski.view.Klotski;

public class MainActivity extends AppCompatActivity {

    private Klotski mKlotskiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Block> blocks = KlotskiMapParser.parse("2,0,0,4,1,0,2,3,0,2,0,2,3,1,2,2,3,2,1,1,3,1,2,3,1,0,4,1,3,4");

        mKlotskiView = (Klotski) findViewById(R.id.main_klotski);
        mKlotskiView.setBlocks(blocks);
    }
}
