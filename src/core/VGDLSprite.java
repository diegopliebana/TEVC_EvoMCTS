package core;

import core.competition.CompetitionParameters;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import ontology.physics.*;
import tools.Utils;
import tools.Vector2d;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 17/10/13
 * Time: 10:59
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public abstract class VGDLSprite {

    /**
     * Name of this sprite.
     */
    public String name;

    /**
     * Indicates if this sprite is static or not.
     */
    public boolean is_static;

    /**
     * Indicates if passive movement is denied for this sprite.
     */
    public boolean only_active;

    /**
     * Indicates if this sprite is the avatar (player) of the game.
     */
    public boolean is_avatar;

    /**
     * Indicates if the sprite has a stochastic behaviour.
     */
    public boolean is_stochastic;

    /**
     * Color of this sprite.
     */
    public Color color;

    /**
     * States the pause ticks in-between two moves
     */
    public int cooldown;

    /**
     * Scalar speed of this sprite.
     */
    public double speed;

    /**
     * Mass of this sprite (for Continuous physics).
     */
    public double mass;

    /**
     * Id of the type if physics this sprite responds to.
     */
    public int physicstype_id;

    /**
     * String that represents the physics type of this sprite.
     */
    public String physicstype;

    /**
     * Reference to the physics object this sprite belongs to.
     */
    public Physics physics;

    /**
     * Scale factor to draw this sprite.
     */
    public double shrinkfactor;

    /**
     * Indicates if this sprite has an oriented behaviour.
     */
    public boolean is_oriented;

    /**
     * Tells if an arrow must be drawn to indicate the orientation of the sprite.
     */
    public boolean draw_arrow;

    /**
     * Orientation of the sprite.
     */
    public Vector2d orientation;

    /**
     * Rectangle that this sprite occupies on the screen.
     */
    public Rectangle rect;

    /**
     * Rectangle occupied for this sprite in the previous game step.
     */
    public Rectangle lastrect;

    /**
     * Tells how many timesteps ago was the last move
     */
    public int lastmove;

    /**
     * Strength measure of this sprite.
     */
    public double strength;

    /**
     * Indicates if this sprite is a singleton.
     */
    public boolean singleton;

    /**
     * Indicates if this sprite is a resource.
     */
    public boolean is_resource;

    /**
     * Indicates if this sprite is a portal.
     */
    public boolean portal;

    /**
     * Indicates if the sprite is invisible. If it is, the effect is that
     * it is not drawn.
     */
    public boolean invisible;

    /**
     * List of types this sprite belongs to. It contains the ids, including itself's, from this sprite up
     * in the hierarchy of sprites defined in SpriteSet in the game definition.
     */
    public ArrayList<Integer> itypes;

    /**
     * Indicates the amount of resources this sprite has, for each type defined as its int identifier.
     */
    public TreeMap<Integer, Integer> resources;

    /**
     * Image of this sprite.
     */
    public Image image;

    /**
     * String that represents the image in VGDL.
     */
    public String img;

    /**
     * Indicates if this sprite is an NPC.
     */
    public boolean is_npc;

    /**
     * ID of this sprite.
     */
    public int spriteID;

    /**
     * Indicates if this sprite was created by the avatar.
     */
    public boolean is_from_avatar;

    /**
     * Bucket
     */
    public int bucket;

    /**
     * Bucket remainder.
     */
    public boolean bucketSharp;

    /**
     * Initializes the sprite, giving its position and dimensions.
     * @param position position of the sprite
     * @param size dimensions of the sprite on the screen.
     */
    protected void init(Vector2d position, Dimension size) {
        this.setRect(position, size);
        this.lastrect = new Rectangle(rect);
        physicstype_id = Types.PHYSICS_GRID;
        physics = null;
        image = null;
        speed = 0;
        cooldown = 0;
        color = null;
        only_active = false;
        name = null;
        is_static = false;
        is_avatar = false;
        is_stochastic = false;
        is_from_avatar = false;
        mass = 1;
        shrinkfactor = 1.0;
        is_oriented = false;
        draw_arrow = false;
        orientation = Types.NONE;
        lastmove = 0;
        invisible = false;
        resources = new TreeMap<Integer, Integer>();
        itypes = new ArrayList<Integer>();

        determinePhysics(physicstype_id, size);
        setRandomColor();
    }

    public void setRect(Vector2d position, Dimension size)
    {
        Rectangle r = new Rectangle((int) position.x, (int) position.y, size.width, size.height);
        setRect(r);
    }


    public void setRect(Rectangle rectangle)
    {
        rect = new Rectangle(rectangle);
        bucket = rect.y / rect.height;
        bucketSharp = (rect.y % rect.height) == 0;
    }

    /**
     * Loads the default values for this sprite.
     */
    protected void loadDefaults() {
        name = this.getClass().getName();
    }

    /**
     * Sets a sampleRandom color for the sprite.
     */
    private void setRandomColor() {
        Random colorRnd = new Random();
        this.color = new Color((Integer) Utils.choice(Types.COLOR_DISC, colorRnd),
                (Integer) Utils.choice(Types.COLOR_DISC, colorRnd),
                (Integer) Utils.choice(Types.COLOR_DISC, colorRnd));
    }


    /**
     * Parses parameters for the sprite, received as a SpriteContent object.
     * @param content
     */
    public void parseParameters(SpriteContent content) {

        VGDLFactory factory = VGDLFactory.GetInstance();
        factory.parseParameters(content,this);

        //post-process. Some sprites may need to do something interesting (i.e. SpawnPoint) once their
        // parameters have been defined.
        this.postProcess();
    }

    /**
     * Determines the physics type of the game, creating the Physics objects that performs the calculations.
     * @param physicstype identifier of the physics type.
     * @param size dimensions of the sprite.
     * @return the phyics object.
     */
    private Physics determinePhysics(int physicstype, Dimension size) {
        this.physicstype_id = physicstype;
        switch (physicstype) {
            case Types.PHYSICS_GRID:
                physics = new GridPhysics(size);
                break;
            case Types.PHYSICS_CONT:
                physics = new ContinuousPhysics();
                break;
            case Types.PHYSICS_NON_FRICTION:
                physics = new NoFrictionPhysics();
                break;
            case Types.PHYSICS_GRAVITY:
                physics = new GravityPhysics();
                break;
        }
        return physics;
    }

    /**
     * Updates this sprite, performing the movements and actions for the next step.
     * @param game the current game that is being played.
     */
    public void update(Game game)
    {
        updatePassive();
    }


    /**
     * Applies both the passive and active movement for the avatar in the forward model.
     */
    public void applyMovement(boolean[] actionMask) {

        //First, lock orientation, if any.
        Vector2d tmp = orientation.copy();
        orientation = Types.NONE.copy();

        //Then, update the passive movement.
        updatePassive();

        //Apply action supplied.
        Vector2d action = Utils.processMovementActionKeys(actionMask);
        this.physics.activeMovement(this, action, this.speed);

        //Set orientation of the avatar.
        Vector2d dir = lastDirection();
        if(dir.x == 0 && dir.y == 0)
        {
            //No movement.
            orientation = tmp;
        }else{
            //moved, update:
            dir.normalise();
            orientation = dir;
        }
    }

    /**
     * Updates this sprite applying the passive movement.
     */
    public void updatePassive() {

        lastrect = new Rectangle(rect);
        lastmove += 1;

        if (!is_static && !only_active) {
            physics.passiveMovement(this);
        }
    }

    /**
     * Updates the position of the sprite, giving its orientation and speed.
     * @param orientation the orientation of the sprite.
     * @param speed the speed of the sprite.
     */
    public void _updatePos(Vector2d orientation, int speed) {
        if (speed == 0) {
            speed = (int) this.speed;
        }

        if (cooldown <= lastmove && (Math.abs(orientation.x) + Math.abs(orientation.y) != 0)) {
            rect.translate((int) orientation.x * speed, (int) orientation.y * speed);
            bucket = rect.y / rect.height;
            bucketSharp = (rect.y % rect.height) == 0;
            lastmove = 0;
        }
    }

    /**
     * Returns the velocity of the sprite, in a Vector2d object.
     * @return the velocity of the sprite
     */
    public Vector2d _velocity() {
        if (speed == 0 || !is_oriented) {
            return new Vector2d(0, 0);
        } else {
            return new Vector2d(orientation.x * speed, orientation.y * speed);
        }
    }

    /**
     * Returns the last direction this sprite is following.
     * @return the direction.
     */
    public Vector2d lastDirection() {
        return new Vector2d(rect.getMinX() - lastrect.getMinX(),
                rect.getMinY() - lastrect.getMinY());
    }

    /**
     * Gets the position of this sprite.
     * @return the position as a Vector2d.
     */
    public Vector2d getPosition()
    {
        return new Vector2d(rect.x, rect.y);
    }

    /**
     * Modifies the amount of resource by a given quantity.
     * @param resourceId id of the resource whose quantity must be changed.
     * @param amount_delta amount of units the resource has to be modified by.
     */
    public void modifyResource(int resourceId, int amount_delta)
    {
        int prev = getAmountResource(resourceId);
        int next = Math.max(0,prev + amount_delta);
        resources.put(resourceId, next);
    }

    /**
     * Returns the amount of resource of a given type this sprite has.
     * @param resourceId id of the resource to check.
     * @return how much of this resource this sprite has.
     */
    public int getAmountResource(int resourceId)
    {
        int prev = 0;
        if(resources.containsKey(resourceId))
            prev = resources.get(resourceId);

        return prev;
    }

    /**
     * Draws this sprite (both the not oriented and, if appropriate, the oriented part)
     * @param gphx graphics object to draw in.
     * @param game reference to the game that is being played now.
     */
    public void draw(Graphics2D gphx, Game game) {

        if(!invisible)
        {
            if(image != null)
                _drawImage(gphx, game);
            else
                _draw(gphx, game);

            if(resources.size() > 0)
            {
                _drawResources(gphx, game);
            }

            if (is_oriented)
                _drawOriented(gphx);
        }
    }

    /**
     * In case this sprite is oriented and has an arrow to draw, it draws it.
     * @param g graphics device to draw in.
     */
    public void _drawOriented(Graphics2D g)
    {
        if(draw_arrow)
        {
            Color arrowColor = new Color(color.getRed(), 255-color.getGreen(), color.getBlue());
            Polygon p = Utils.triPoints(rect, orientation);

            g.setColor(arrowColor);
            //g.drawPolygon(p);
            g.fillPolygon(p);
        }
    }

    /**
     * Draws the not-oriented part of the sprite
     * @param gphx graphics object to draw in.
     * @param game reference to the game that is being played now.
     */
    public void _draw(Graphics2D gphx, Game game)
    {
        Rectangle r = new Rectangle(rect);
        if(shrinkfactor != 1)
        {
            r.width *= shrinkfactor;
            r.height *= shrinkfactor;
            r.x += (rect.width-r.width)/2;
            r.y += (rect.height-r.height)/2;
        }

        gphx.setColor(color);

        if(is_avatar)
        {
            gphx.fillOval((int) r.getX(), (int) r.getY(), r.width, r.height);
        }else if(!is_static)
        {
            gphx.fillRect(r.x, r.y, r.width, r.height);
        }else
        {
            gphx.fillRect(r.x, r.y, r.width, r.height);
        }
    }

    /**
     * Draws the not-oriented part of the sprite, as an image. this.image must be not null.
     * @param gphx graphics object to draw in.
     * @param game reference to the game that is being played now.
     */
    public void _drawImage(Graphics2D gphx, Game game)
    {
        Rectangle r = new Rectangle(rect);
        if(shrinkfactor != 1)
        {
            r.width *= shrinkfactor;
            r.height *= shrinkfactor;
            r.x += (rect.width-r.width)/2;
            r.y += (rect.height-r.height)/2;
        }

        int w = image.getWidth(null);
        int h = image.getHeight(null);
        float scale = (float)r.width/w; //assume all sprites are quadratic.

        gphx.drawImage(image, r.x, r.y, (int) (w*scale), (int) (h*scale), null);

        //uncomment this to see lots of numbers around
        //gphx.setColor(Color.BLACK);
        //if(bucketSharp)   gphx.drawString("["+bucket+"]",r.x, r.y);
        //else              gphx.drawString("{"+bucket+"}",r.x, r.y);


    }

    /**
     * Draws the resources hold by this sprite, as an horizontal bar on top of the sprite.
     * @param gphx graphics to draw in.
     * @param game game being played at the moment.
     */
    protected void _drawResources(Graphics2D gphx, Game game)
    {
        int numResources = resources.size();
        double barheight = rect.getHeight() / 3.5f / numResources;
        double offset = rect.getMinY() + 2*rect.height / 3.0f;

        Set<Map.Entry<Integer, Integer>> entries = resources.entrySet();
        for(Map.Entry<Integer, Integer> entry : entries)
        {
            int resType = entry.getKey();
            int resValue = entry.getValue();

            double wiggle = rect.width/10.0f;
            double prop = Math.max(0,Math.min(1,resValue / (double)(game.getResourceLimit(resType))));

            Rectangle filled = new Rectangle((int) (rect.x+wiggle/2), (int) offset, (int) (prop*(rect.width-wiggle)), (int) barheight);
            Rectangle rest   = new Rectangle((int) (rect.x+wiggle/2+prop*(rect.width-wiggle)), (int)offset, (int) ((1-prop)*(rect.width-wiggle)), (int)barheight);

            gphx.setColor(game.getResourceColor(resType));
            gphx.fillRect(filled.x, filled.y, filled.width, filled.height);
            gphx.setColor(Types.BLACK);
            gphx.fillRect(rest.x, rest.y, rest.width, rest.height);
            offset += barheight;
        }

    }

    /**
     * Gets the unique and precise type of this sprite
     * @return the type
     */
    public int getType()
    {
        return itypes.get(itypes.size()-1);
    }

    /**
     * Method to perform post processing when the sprite has received its parameters.
     */
    public void postProcess()
    {
        loadImage(img);

        if(this.orientation != Types.NONE)
        {
            //Any sprite that receives an orientation, is oriented.
            this.is_oriented = true;
        }
    }

    /**
     * Loads the image that represents this sprite, using its string name as reference.
     * @param str name of the image to load.
     */
    public void loadImage(String str)
    {
        if(image == null && str != null)
        {
            //load image.
            try {
                if (!(str.contains(".png"))) str = str + ".png";
                String image_file = CompetitionParameters.IMG_PATH + str;
                if((new File(image_file).exists())) {
                    image = ImageIO.read(new File(image_file));
                }
                else {
                    //System.out.println(image_file);
                    image = ImageIO.read(this.getClass().getResource("/" + image_file));
                }

            } catch (IOException e) {
                System.out.println("Image " + str + " could not be found.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Used to indicate if this sprite was created by the avatar.
     * @param fromAvatar true if the avatar created this sprite.
     */
    public void setFromAvatar(boolean fromAvatar)
    {
        is_from_avatar = fromAvatar;
    }


    /**
     * Returns a string representation of this string, including its name and position.
     * @return the string representation of this sprite.
     */
    public String toString() {
        return name + " at (" + rect.getMinX() + "," + rect.getMinY() + ")";
    }

    /**
     * Creates a copy of this sprite. To be overwritten in each subclass.
     * @return  a copy of this sprite.
     */
    public abstract VGDLSprite copy();

    /**
     * Copies the attributes of this object to the one passed as parameter.
     * @param toSprite the sprite to copy to.
     */
    public void copyTo(VGDLSprite toSprite)
    {
        //this.color, this.draw_arrow don't need to be copied.
        toSprite.name = this.name;
        toSprite.is_static = this.is_static;
        toSprite.only_active = this.only_active;
        toSprite.is_avatar = this.is_avatar;
        toSprite.is_stochastic = this.is_stochastic;
        toSprite.cooldown = this.cooldown;
        toSprite.speed = this.speed;
        toSprite.mass = this.mass;
        toSprite.physicstype_id = this.physicstype_id;
        toSprite.physics = this.physics; //Object reference, but should be ok.
        toSprite.shrinkfactor = this.shrinkfactor;
        toSprite.is_oriented = this.is_oriented;
        toSprite.orientation = this.orientation.copy();
        toSprite.rect = new Rectangle(this.rect.x, this.rect.y, this.rect.width, this.rect.height);
        toSprite.lastrect =  new Rectangle(this.lastrect.x, this.lastrect.y, this.lastrect.width, this.lastrect.height);
        toSprite.lastmove = this.lastmove;
        toSprite.strength = this.strength;
        toSprite.singleton = this.singleton;
        toSprite.is_resource = this.is_resource;
        toSprite.portal = this.portal;
        toSprite.physicstype = this.physicstype;
        toSprite.color = this.color;
        toSprite.draw_arrow = this.draw_arrow;
        toSprite.is_npc = this.is_npc;
        toSprite.image = this.image;
        toSprite.spriteID = this.spriteID;
        toSprite.is_from_avatar = this.is_from_avatar;
        toSprite.bucket = this.bucket;
        toSprite.bucketSharp = this.bucketSharp;

        toSprite.itypes = new ArrayList<Integer>();
        for(Integer it : this.itypes)
            toSprite.itypes.add(it);

        toSprite.resources = new TreeMap<Integer, Integer>();
        Set<Map.Entry<Integer, Integer>> entries = this.resources.entrySet();
        for(Map.Entry<Integer, Integer> entry : entries)
        {
            toSprite.resources.put(entry.getKey(), entry.getValue());
        }

    }

}
