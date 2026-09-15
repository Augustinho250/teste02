package com.gamedevai.dungeonarena;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameView extends View {
    private enum State { MENU, PLAYING, PAUSED, GAME_OVER }
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random rng = new Random();
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private final ArrayList<Loot> loot = new ArrayList<>();
    private final SharedPreferences prefs;
    private State state = State.MENU;
    private float w,h,px,py,moveX,moveY;
    private int hp,maxHp,level,xp,xpNext,coins,score,wave,kills,best;
    private float speed,damage,range;
    private long cooldown,lastAttack,lastSpawn,lastFrame,waveStart;
    private boolean bossSpawned;

    public GameView(Context c){
        super(c); setFocusable(true); setKeepScreenOn(true);
        prefs=c.getSharedPreferences("dungeon_arena_save",Context.MODE_PRIVATE);
        best=prefs.getInt("best",0); coins=prefs.getInt("coins",0);
    }

    private void newGame(){
        enemies.clear(); loot.clear(); level=1; xp=0; xpNext=60; maxHp=100; hp=100;
        speed=320; damage=25; range=120; cooldown=450; score=0; wave=1; kills=0; bossSpawned=false;
        px=w/2f; py=h*0.55f; long n=System.currentTimeMillis(); lastFrame=lastSpawn=waveStart=n;
        state=State.PLAYING; invalidate();
    }

    public void pauseGame(){ if(state==State.PLAYING) state=State.PAUSED; }
    public void resumeGame(){ if(state==State.PLAYING){lastFrame=System.currentTimeMillis();invalidate();} }

    @Override protected void onSizeChanged(int ww,int hh,int ow,int oh){w=ww;h=hh;if(px==0&&py==0){px=w/2f;py=h/2f;}}

    @Override protected void onDraw(Canvas c){
        super.onDraw(c); drawBg(c);
        if(state==State.MENU){drawMenu(c);return;}
        long now=System.currentTimeMillis(); float dt=Math.min(.033f,Math.max(.001f,(now-lastFrame)/1000f)); lastFrame=now;
        if(state==State.PLAYING) update(dt,now);
        drawArena(c); drawHud(c); drawControls(c);
        if(state==State.PAUSED) overlay(c,"PAUSADO","Toque para continuar");
        if(state==State.GAME_OVER) overlay(c,"FIM DE JOGO","Toque para jogar novamente");
        if(state==State.PLAYING) postInvalidateOnAnimation();
    }

    private void update(float dt,long now){
        px=clamp(px+moveX*speed*dt,35,w-35); py=clamp(py+moveY*speed*dt,75,h-35);
        long every=Math.max(420,1150-wave*55L); int max=Math.min(28,5+wave*2);
        if(now-lastSpawn>=every&&enemies.size()<max){spawn(false);lastSpawn=now;}
        if(wave%5==0&&!bossSpawned&&now-waveStart>2500){spawn(true);bossSpawned=true;}
        if(kills>=8+wave*3){wave++;kills=0;bossSpawned=false;waveStart=now;hp=Math.min(maxHp,hp+20);}
        for(Enemy e:enemies){
            float dx=px-e.x,dy=py-e.y,d=(float)Math.sqrt(dx*dx+dy*dy);
            if(d>.1f){e.x+=dx/d*e.speed*dt;e.y+=dy/d*e.speed*dt;}
            if(d<e.r+26&&now-e.lastHit>650){hp-=e.hit;e.lastHit=now;if(hp<=0){hp=0;gameOver();break;}}
        }
        Iterator<Loot> it=loot.iterator();
        while(it.hasNext()){
            Loot l=it.next(); float dx=px-l.x,dy=py-l.y;
            if(dx*dx+dy*dy<55*55){if(l.type==0){coins+=l.value;score+=l.value*5;}else hp=Math.min(maxHp,hp+l.value);it.remove();}
        }
    }

    private void spawn(boolean boss){
        Enemy e=new Enemy(); int edge=rng.nextInt(4);
        if(edge==0){e.x=-30;e.y=80+rng.nextFloat()*Math.max(10,h-120);} else if(edge==1){e.x=w+30;e.y=80+rng.nextFloat()*Math.max(10,h-120);} else if(edge==2){e.x=rng.nextFloat()*Math.max(10,w);e.y=65;} else {e.x=rng.nextFloat()*Math.max(10,w);e.y=h+30;}
        float s=1f+(wave-1)*.12f; e.boss=boss;
        if(boss){e.r=52;e.hp=e.maxHp=380*s;e.speed=72+wave*3.5f;e.hit=18+wave*2;} else {e.r=25+rng.nextFloat()*7;e.hp=e.maxHp=(45+wave*10)*(0.85f+rng.nextFloat()*.3f);e.speed=95+wave*4+rng.nextFloat()*35;e.hit=7+wave;}
        enemies.add(e);
    }

    private void attack(long now){
        if(state!=State.PLAYING||now-lastAttack<cooldown)return; lastAttack=now;
        Enemy nearest=null;float bestD=Float.MAX_VALUE;
        for(Enemy e:enemies){float dx=e.x-px,dy=e.y-py,d=dx*dx+dy*dy;if(d<bestD){bestD=d;nearest=e;}}
        if(nearest==null)return;
        float tx=nearest.x-px,ty=nearest.y-py,td=(float)Math.sqrt(tx*tx+ty*ty); if(td>range+nearest.r)return;
        Iterator<Enemy> it=enemies.iterator();
        while(it.hasNext()){
            Enemy e=it.next();float dx=e.x-px,dy=e.y-py,d=(float)Math.sqrt(dx*dx+dy*dy);
            if(d<=range+e.r){e.hp-=damage;if(e.hp<=0){kill(e);it.remove();}}
        }
    }

    private void kill(Enemy e){
        kills++; int gain=e.boss?120:15; score+=e.boss?500:75; xp+=gain;
        loot.add(new Loot(e.x,e.y,0,e.boss?20+wave*2:1+rng.nextInt(4)));
        if(rng.nextFloat()<(e.boss?.75f:.08f)) loot.add(new Loot(e.x+20,e.y,1,e.boss?40:20));
        while(xp>=xpNext){xp-=xpNext;level++;xpNext=(int)(xpNext*1.28f+20);maxHp+=14;hp=Math.min(maxHp,hp+35);damage+=6;speed+=8;if(level%3==0)cooldown=Math.max(220,cooldown-25);if(level%2==0)range+=4;}
    }

    private void gameOver(){state=State.GAME_OVER;if(score>best)best=score;prefs.edit().putInt("best",best).putInt("coins",coins).apply();invalidate();}

    private void drawBg(Canvas c){
        c.drawColor(Color.rgb(17,20,27));p.setStrokeWidth(2);p.setColor(Color.rgb(31,36,47));
        for(float x=0;x<w;x+=58)c.drawLine(x,0,x,h,p);for(float y=0;y<h;y+=58)c.drawLine(0,y,w,y,p);
    }

    private void drawArena(Canvas c){
        for(Loot l:loot){p.setColor(l.type==0?Color.rgb(255,213,79):Color.rgb(214,58,72));c.drawCircle(l.x,l.y,l.type==0?10:12,p);}
        for(Enemy e:enemies){p.setColor(e.boss?Color.rgb(137,77,200):Color.rgb(198,67,67));c.drawCircle(e.x,e.y,e.r,p);float bw=e.r*1.8f,ratio=Math.max(0,e.hp/e.maxHp);p.setColor(Color.rgb(60,25,30));c.drawRect(e.x-bw/2,e.y-e.r-14,e.x+bw/2,e.y-e.r-8,p);p.setColor(Color.rgb(93,211,113));c.drawRect(e.x-bw/2,e.y-e.r-14,e.x-bw/2+bw*ratio,e.y-e.r-8,p);}
        p.setColor(Color.rgb(49,132,214));c.drawCircle(px,py,27,p);p.setColor(Color.rgb(164,219,255));c.drawCircle(px-7,py-7,7,p);
    }

    private void drawHud(Canvas c){
        p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.LEFT);p.setTextSize(23);p.setColor(Color.WHITE);c.drawText("Nível "+level+"   Onda "+wave,22,32,p);
        float bw=Math.min(350,w*.38f);p.setColor(Color.rgb(64,29,32));c.drawRoundRect(22,43,22+bw,59,8,8,p);p.setColor(Color.rgb(220,70,77));c.drawRoundRect(22,43,22+bw*(hp/(float)maxHp),59,8,8,p);
        p.setColor(Color.rgb(35,52,76));c.drawRoundRect(22,66,22+bw,76,5,5,p);p.setColor(Color.rgb(76,157,235));c.drawRoundRect(22,66,22+bw*(xp/(float)xpNext),76,5,5,p);
        p.setTextAlign(Paint.Align.RIGHT);p.setTextSize(20);p.setColor(Color.rgb(255,216,83));c.drawText("Moedas: "+coins,w-22,32,p);p.setColor(Color.WHITE);c.drawText("Pontos: "+score,w-22,58,p);
        if(wave%5==0){p.setTextAlign(Paint.Align.CENTER);p.setColor(Color.rgb(213,167,255));c.drawText("ONDA DE BOSS",w/2f,31,p);}
    }

    private void drawControls(Canvas c){
        float jx=105,jy=h-105;p.setColor(Color.argb(70,255,255,255));c.drawCircle(jx,jy,68,p);p.setColor(Color.argb(130,255,255,255));c.drawCircle(jx+moveX*34,jy+moveY*34,30,p);
        float bx=w-105,by=h-105;p.setColor(Color.argb(190,205,73,73));c.drawCircle(bx,by,58,p);p.setColor(Color.WHITE);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(18);c.drawText("ATACAR",bx,by+7,p);
    }

    private void drawMenu(Canvas c){
        p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);p.setColor(Color.rgb(255,216,83));p.setTextSize(Math.min(58,w*.08f));c.drawText("DUNGEON ARENA",w/2f,h*.28f,p);
        p.setColor(Color.WHITE);p.setTextSize(21);c.drawText("RPG de arena • sobreviva • evolua • derrote bosses",w/2f,h*.39f,p);
        RectF b=new RectF(w/2f-165,h*.51f,w/2f+165,h*.51f+72);p.setColor(Color.rgb(49,132,214));c.drawRoundRect(b,22,22,p);p.setColor(Color.WHITE);p.setTextSize(27);c.drawText("JOGAR",w/2f,h*.51f+45,p);
        p.setTextSize(18);p.setColor(Color.LTGRAY);c.drawText("Recorde: "+best+"     Moedas: "+coins,w/2f,h*.75f,p);
    }

    private void overlay(Canvas c,String a,String b){
        p.setColor(Color.argb(205,8,10,14));c.drawRect(0,0,w,h,p);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);p.setTextSize(46);p.setColor(Color.WHITE);c.drawText(a,w/2f,h*.4f,p);p.setTextSize(22);c.drawText(b,w/2f,h*.53f,p);
    }

    @Override public boolean onTouchEvent(MotionEvent e){
        int a=e.getActionMasked();float x=e.getX(),y=e.getY();long now=System.currentTimeMillis();
        if(state==State.MENU){if(a==MotionEvent.ACTION_DOWN)newGame();return true;}
        if(state==State.GAME_OVER){if(a==MotionEvent.ACTION_DOWN)newGame();return true;}
        if(state==State.PAUSED){if(a==MotionEvent.ACTION_DOWN){state=State.PLAYING;lastFrame=now;invalidate();}return true;}
        if(a==MotionEvent.ACTION_DOWN||a==MotionEvent.ACTION_MOVE){
            if(x>w*.62f&&y>h*.48f){attack(now);}
            else if(x<w*.48f){float dx=x-105,dy=y-(h-105),d=(float)Math.sqrt(dx*dx+dy*dy);if(d>1){moveX=dx/Math.max(68,d);moveY=dy/Math.max(68,d);} }
        }
        if(a==MotionEvent.ACTION_UP||a==MotionEvent.ACTION_CANCEL){moveX=moveY=0;}
        return true;
    }

    private static float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
    private static class Enemy{float x,y,r,hp,maxHp,speed;int hit;long lastHit;boolean boss;}
    private static class Loot{float x,y;int type,value;Loot(float x,float y,int type,int value){this.x=x;this.y=y;this.type=type;this.value=value;}}
}
