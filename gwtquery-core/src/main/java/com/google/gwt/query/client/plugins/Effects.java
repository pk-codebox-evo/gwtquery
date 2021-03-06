/*
 * Copyright 2011, The gwtquery team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.query.client.plugins;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Properties;
import com.google.gwt.query.client.plugins.effects.ClipAnimation;
import com.google.gwt.query.client.plugins.effects.ClipAnimation.Action;
import com.google.gwt.query.client.plugins.effects.ClipAnimation.Direction;
import com.google.gwt.query.client.plugins.effects.Fx;
import com.google.gwt.query.client.plugins.effects.PropertiesAnimation;
import com.google.gwt.query.client.plugins.effects.PropertiesAnimation.Easing;
import com.google.gwt.query.client.plugins.effects.PropertiesAnimation.EasingCurve;
import com.google.gwt.query.client.plugins.effects.TransitionsAnimation;
import com.google.gwt.query.client.plugins.effects.TransitionsAnimation.TransitionsClipAnimation;

/**
 * Effects plugin for Gwt Query.
 */
public class Effects extends QueuePlugin<Effects> {

  /**
   * Class to access protected methods in Animation.
   */
  public static abstract class GQAnimation  extends Animation {

    private static final String ACTUAL_ANIMATION = "EffectsRunnning";

    // Each Animation is associated to one element
    protected Element e;
    protected Properties prps;

    protected GQAnimation setElement(Element element) {
      e = element;
      return this;
    }
    protected GQAnimation setProperties(Properties properties) {
      prps = properties == null ? Properties.create() : properties;
      return this;
    }
    protected void onStart() {
      // Mark this animation as actual, so as we can stop it in the GQuery.stop() method
      $(e).data(ACTUAL_ANIMATION, this);
      super.onStart();
    }
    protected void onComplete() {
      // avoid memory leak (issue #132)
      $(e).removeData(ACTUAL_ANIMATION);
      super.onComplete();
    }
    public void cancel() {
      // avoid memory leak (issue #132)
      $(e).removeData(ACTUAL_ANIMATION);
      super.cancel();
    }
  }

  /**
   * Just a class to store predefined speed constant values.
   */
  public static class Speed {
    public static final int DEFAULT = 400;
    public static final int FAST = 200;
    public static final int SLOW = 600;
  }

  public static final Class<Effects> Effects = GQuery.registerPlugin(
      Effects.class, new Plugin<Effects>() {
        public Effects init(GQuery gq) {
          return new Effects(gq);
        }
      });

  protected Effects(GQuery gq) {
    super(gq);
  }

  private void queueAnimation(final Element e, final GQAnimation anim, final int duration) {
    if (isOff()) {
      anim.onStart();
      anim.onComplete();
    } else {
      queue(e, DEFAULT_NAME, new Function() {
        public void cancel(Element e) {
          Animation anim = (Animation) data(e, GQAnimation.ACTUAL_ANIMATION, null);
          if (anim != null) {
            anim.cancel();
          }
        }
        public void f(Element e) {
          anim.run(duration);
        }
      });
    }
  }

  protected boolean isOff() {
    return Fx.off;
  }

  /**
   * The animate() method allows you to create animation effects on any numeric
   * Attribute, CSS property, or color CSS property.
   *
   * Concerning to numeric properties, values are treated as a number of pixels
   * unless otherwise specified. The units em and % can be specified where
   * applicable.
   *
   * By default animate considers css properties, if you wanted to animate element
   * attributes you should to prepend the symbol dollar to the attribute name.
   *
   * Example:
   *
   * <pre class="code">
   *  //move the element from its original position to the position top:500px and left:500px for 400ms.
   *  //use a swing easing function for the transition
   *  $("#foo").animate(Properties.create("{top:'500px',left:'500px'}"), 400, EasingCurve.swing);
   *  // Change the width and border attributes of a table
   *  $("table").animate(Properties.create("{$width: '500', $border: '10'}"), 400, EasingCurve.linear);
   * </pre>
   *
   * In addition to numeric values, each property can take the strings 'show',
   * 'hide', and 'toggle'. These shortcuts allow for custom hiding and showing
   * animations that take into account the display type of the element. Animated
   * properties can also be relative. If a value is supplied with a leading +=
   * or -= sequence of characters, then the target value is computed by adding
   * or subtracting the given number from the current value of the property.
   *
   * Example:
   *
   * <pre class="code">
   *  //move the element from its original position to 500px to the left and 5OOpx down for 400ms.
   *  //use a swing easing function for the transition
   *  $("#foo").animate(Properties.create("{top:'+=500px',left:'+=500px'}"), 400, Easing.SWING);
   * </pre>
   *
   * For color css properties, values can be specified via hexadecimal or rgb or
   * literal values.
   *
   * Example:
   *
   * <pre class="code">
   *  $("#foo").animate("backgroundColor:'red', color:'#ffffff', borderColor:'rgb(129, 0, 70)'", 400, EasingCurve.swing);
   *  $("#foo").animate($$("{backgroundColor:'red', color:'#ffffff', borderColor:'rgb(129, 0, 70)'}"), 400, EasingCurve.swing);
   * </pre>
   *
   * @param p a {@link Properties} object containing css properties to animate.
   * @param funcs an array of {@link Function} called once the animation is
   *          complete
   * @param duration the duration in milliseconds of the animation
   * @param easing the easing function to use for the transition
   */
  public Effects animate(Object stringOrProperties, final int duration,
      final Easing easing, final Function... funcs) {


    final Properties p = (stringOrProperties instanceof String)
        ? $$((String) stringOrProperties) : (Properties) stringOrProperties;

    for (Element e: elements()) {
      if (Fx.css3) {
        new TransitionsAnimation(easing, e, p, funcs).run(duration);
      } else {
        queueAnimation(e, new PropertiesAnimation(easing, e, p, funcs), duration);
      }
    }
    return this;
  }

  /**
   *
   * The animate() method allows you to create animation effects on any numeric
   * Attribute, CSS property, or color CSS property.
   *
   * Concerning to numeric properties, values are treated as a number of pixels
   * unless otherwise specified. The units em and % can be specified where
   * applicable.
   *
   * By default animate considers css properties, if you wanted to animate element
   * attributes you should to prepend the symbol dollar to the attribute name.
   *
   * Example:
   *
   * <pre class="code">
   *  //move the element from its original position to left:500px for 500ms
   *  $("#foo").animate("left:'500'");
   *  // Change the width attribute of a table
   *  $("table").animate("$width:'500'"), 400, EasingCurve.swing);
   * </pre>
   *
   * In addition to numeric values, each property can take the strings 'show',
   * 'hide', and 'toggle'. These shortcuts allow for custom hiding and showing
   * animations that take into account the display type of the element. Animated
   * properties can also be relative. If a value is supplied with a leading +=
   * or -= sequence of characters, then the target value is computed by adding
   * or subtracting the given number from the current value of the property.
   *
   * Example:
   *
   * <pre class="code">
   *  //move the element from its original position to 500px to the left for 500ms and
   *  // change the background color of the element at the end of the animation
   *  $("#foo").animate("left:'+=500'", new Function(){
   *
   *                 public void f(Element e){
   *                   $(e).css(CSS.BACKGROUND_COLOR.with(RGBColor.RED);
   *                 }
   *
   *              });
   * </pre>
   *
   * The duration of the animation is 500ms.
   *
   * For color css properties, values can be specified via hexadecimal or rgb or
   * literal values.
   *
   * Example:
   *
   * <pre class="code">
   *  $("#foo").animate("backgroundColor:'red', color:'#ffffff', borderColor:'rgb(129, 0, 70)'");
   * </pre>
   *
   * @param prop the property to animate : "cssName:'value'"
   * @param funcs an array of {@link Function} called once the animation is
   *          complete
   */
  public Effects animate(Object stringOrProperties, Function... funcs) {
    return animate(stringOrProperties, 500, funcs);
  }

  /**
   * The animate() method allows you to create animation effects on any numeric
   * Attribute, CSS properties, or color CSS property.
   *
   * Concerning to numeric property, values are treated as a number of pixels
   * unless otherwise specified. The units em and % can be specified where
   * applicable.
   *
   * By default animate considers css properties, if you wanted to animate element
   * attributes you should to prepend the symbol dollar to the attribute name.
   *
   * Example:
   *
   * <pre class="code">
   *  //move the element from its original position to left:500px for 2s
   *  $("#foo").animate("left:'500px'", 2000);
   *  // Change the width attribute of a table
   *  $("table").animate("$width:'500'"), 400);
   * </pre>
   *
   * In addition to numeric values, each property can take the strings 'show',
   * 'hide', and 'toggle'. These shortcuts allow for custom hiding and showing
   * animations that take into account the display type of the element. Animated
   * properties can also be relative. If a value is supplied with a leading +=
   * or -= sequence of characters, then the target value is computed by adding
   * or subtracting the given number from the current value of the property.
   *
   * Example:
   *
   * <pre class="code">
   *  //move the element from its original position to 500px to the left for 1000ms and
   *  // change the background color of the element at the end of the animation
   *  $("#foo").animate("left:'+=500'", 1000, new Function(){
   *     public void f(Element e){
   *       $(e).css(CSS.BACKGROUND_COLOR.with(RGBColor.RED);
   *     }
   *  });
   * </pre>
   *
   *
   * For color css properties, values can be specified via hexadecimal or rgb or
   * literal values.
   *
   * Example:
   *
   * <pre class="code">
   *  $("#foo").animate("backgroundColor:'red', color:'#ffffff', borderColor:'rgb(129, 0, 70)', 1000");
   * </pre>
   *
   *
   * @param prop the property to animate : "cssName:'value'"
   * @param funcs an array of {@link Function} called once the animation is
   *          complete
   * @param duration the duration in milliseconds of the animation
   */
  public Effects animate(Object stringOrProperties, int duration, Function... funcs) {
    return animate(stringOrProperties, duration, EasingCurve.linear, funcs);
  }

  /**
   * Animate the set of matched elements using the clip property. It is possible
   * to show or hide a set of elements, specify the direction of the animation
   * and the start corner of the effect. Finally it executes the set of
   * functions passed as arguments.
   */
  public Effects clip(ClipAnimation.Action a, ClipAnimation.Corner c,
      ClipAnimation.Direction d, Function... f) {
    return clip(a, c, d, Speed.DEFAULT, f);
  }

  /**
   * Animate the set of matched elements using the clip property. It is possible
   * to show or hide a set of elements, specify the direction of the animation
   * and the start corner of the effect. Finally it executes the set of
   * functions passed as arguments.
   */
  public Effects clip(final ClipAnimation.Action a,
      final ClipAnimation.Corner c, final ClipAnimation.Direction d,
      final int duration, final Function... f) {
    for (Element e : elements()) {
      if (Fx.css3) {
        new TransitionsClipAnimation(e, a, c, d, null, null, f).run(duration);
      } else {
        queueAnimation(e, new ClipAnimation(e, a, c, d, f), duration);
      }
    }
    return this;
  }

  /**
   * Animate the set of matched elements using the clip property. It is possible
   * to show or hide a set of elements, specify the direction of the animation
   * and the start corner of the effect. Finally it executes the set of
   * functions passed as arguments.
   */
  public Effects clip(ClipAnimation.Action a, ClipAnimation.Corner c,
      Function... f) {
    return clip(a, c, Direction.BIDIRECTIONAL, Speed.DEFAULT, f);
  }

  /**
   * Reveal all matched elements by adjusting the clip property firing an
   * optional callback after completion. The effect goes from the center to the
   * perimeter.
   */
  public Effects clipAppear(Function... f) {
    return clipAppear(Speed.DEFAULT, f);
  }

  /**
   * Reveal all matched elements by adjusting the clip property firing an
   * optional callback after completion. The effect goes from the center to the
   * perimeter.
   */
  public Effects clipAppear(int millisecs, Function... f) {
    return clip(ClipAnimation.Action.SHOW, ClipAnimation.Corner.CENTER,
        ClipAnimation.Direction.BIDIRECTIONAL, millisecs, f);
  }

  /**
   * Hide all matched elements by adjusting the clip property firing an optional
   * callback after completion. The effect goes from the perimeter to the
   * center.
   */
  public Effects clipDisappear(Function... f) {
    return clipDisappear(Speed.DEFAULT, f);
  }

  /**
   * Hide all matched elements by adjusting the clip property firing an optional
   * callback after completion. The effect goes from the perimeter to the
   * center.
   */
  public Effects clipDisappear(int millisecs, Function... f) {
    return clip(ClipAnimation.Action.HIDE, ClipAnimation.Corner.CENTER,
        ClipAnimation.Direction.BIDIRECTIONAL, millisecs, f);
  }

  /**
   * Reveal all matched elements by adjusting the clip property firing an
   * optional callback after completion. The effect goes from the top to the
   * bottom.
   */
  public Effects clipDown(Function... f) {
    return clipDown(Speed.DEFAULT, f);
  }

  /**
   * Reveal all matched elements by adjusting the clip property firing an
   * optional callback after completion. The effect goes from the top to the
   * bottom.
   */
  public Effects clipDown(int millisecs, Function... f) {
    return clip(Action.SHOW, ClipAnimation.Corner.TOP_LEFT,
        ClipAnimation.Direction.BIDIRECTIONAL, millisecs, f);
  }

  /**
   * Toggle the visibility of all matched elements by adjusting the clip
   * property and firing an optional callback after completion. The effect goes
   * from the bottom to the top.
   */
  public Effects clipToggle(Function... f) {
    return clipToggle(Speed.DEFAULT, f);
  }

  /**
   * Toggle the visibility of all matched elements by adjusting the clip
   * property and firing an optional callback after completion. The effect goes
   * from the bottom to the top.
   */
  public Effects clipToggle(int millisecs, Function... f) {
    return clip(Action.TOGGLE, ClipAnimation.Corner.TOP_LEFT,
        ClipAnimation.Direction.VERTICAL, millisecs, f);
  }

  /**
   * Hide all matched elements by adjusting the clip property firing an optional
   * callback after completion. The effect goes from the bottom to the top.
   */
  public Effects clipUp(Function... f) {
    return clipUp(Speed.DEFAULT, f);
  }

  /**
   * Hide all matched elements by adjusting the clip property firing an optional
   * callback after completion. The effect goes from the bottom to the top.
   */
  public Effects clipUp(int millisecs, Function... f) {
    return clip(Action.HIDE, ClipAnimation.Corner.TOP_LEFT,
        ClipAnimation.Direction.BIDIRECTIONAL, millisecs, f);
  }

  /**
   * Fade in all matched elements by adjusting their opacity and firing an
   * optional callback after completion. Only the opacity is adjusted for this
   * animation, meaning that all of the matched elements should already have
   * some form of height and width associated with them.
   */
  public Effects fadeIn(Function... f) {
    return fadeIn(Speed.DEFAULT, f);
  }

  /**
   * Fade in all matched elements by adjusting their opacity and firing an
   * optional callback after completion. Only the opacity is adjusted for this
   * animation, meaning that all of the matched elements should already have
   * some form of height and width associated with them.
   */
  public Effects fadeIn(int millisecs, Function... f) {
    return animate("opacity: 'show'", millisecs, f);
  }

  /**
   * Fade out all matched elements by adjusting their opacity to 0, then setting
   * display to "none" and firing an optional callback after completion. Only
   * the opacity is adjusted for this animation, meaning that all of the matched
   * elements should already have some form of height and width associated with
   * them.
   */
  public Effects fadeOut(Function... f) {
    return fadeOut(Speed.DEFAULT, f);
  }

  /**
   * Fade out all matched elements by adjusting their opacity to 0, then setting
   * display to "none" and firing an optional callback after completion. Only
   * the opacity is adjusted for this animation, meaning that all of the matched
   * elements should already have some form of height and width associated with
   * them.
   */
  public Effects fadeOut(int millisecs, Function... f) {
    return animate("opacity: 'hide'", millisecs, f);
  };

  /**
   * Fade the opacity of all matched elements to a specified opacity and firing
   * an optional callback after completion. Only the opacity is adjusted for
   * this animation, meaning that all of the matched elements should already
   * have some form of height and width associated with them.
   */
  public Effects fadeTo(double opacity, Function... f) {
    return fadeTo(Speed.DEFAULT, opacity, f);
  }

  /**
   * Fade the opacity of all matched elements to a specified opacity and firing
   * an optional callback after completion. Only the opacity is adjusted for
   * this animation, meaning that all of the matched elements should already
   * have some form of height and width associated with them.
   */
  public Effects fadeTo(int millisecs, double opacity, Function... f) {
    return animate("opacity: " + opacity, millisecs, f);
  }

  /**
   * Display or hide the matched elements by animating their opacity. and firing
   * an optional callback after completion. Only the opacity is adjusted for
   * this animation, meaning that all of the matched elements should already
   * have some form of height and width associated with them.
   */
  public Effects fadeToggle(Function... f) {
    return fadeToggle(Speed.DEFAULT, f);
  }

  /**
   * Display or hide the matched elements by animating their opacity. and firing
   * an optional callback after completion. Only the opacity is adjusted for
   * this animation, meaning that all of the matched elements should already
   * have some form of height and width associated with them.
   */
  public Effects fadeToggle(int millisecs, Function... f) {
    return animate("opacity: 'toggle'", millisecs, f);
  };

  /**
   * Reveal all matched elements by adjusting their height and firing an
   * optional callback after completion.
   */
  public Effects slideDown(Function... f) {
    return slideDown(Speed.DEFAULT, f);
  }

  /**
   * Reveal all matched elements by adjusting their height and firing an
   * optional callback after completion.
   */
  public Effects slideDown(int millisecs, Function... f) {
    return animate("height: 'show'", millisecs, f);
  }

  /**
   * Hide all matched elements by adjusting their width and firing an optional
   * callback after completion.
   */
  public Effects slideLeft(Function... f) {
    return slideLeft(Speed.DEFAULT, f);
  }

  /**
   * Hide all matched elements by adjusting their width and firing an optional
   * callback after completion.
   */
  public Effects slideLeft(int millisecs, Function... f) {
    return animate("width: 'hide'", millisecs, f);
  }

  /**
   * Reveal all matched elements by adjusting their width and firing an optional
   * callback after completion.
   */
  public Effects slideRight(Function... f) {
    return slideRight(Speed.DEFAULT, f);
  }

  /**
   * Reveal all matched elements by adjusting their width and firing an optional
   * callback after completion.
   */
  public Effects slideRight(final int millisecs, Function... f) {
    return animate("width: 'show'", millisecs, f);
  }

  /**
   * Toggle the visibility of all matched elements by adjusting their height and
   * firing an optional callback after completion. Only the height is adjusted
   * for this animation, causing all matched elements to be hidden or shown in a
   * "sliding" manner
   */
  public Effects slideToggle(int millisecs, Function... f) {
    return animate("height: 'toggle'", millisecs, f);
  }

  /**
   * Hide all matched elements by adjusting their height and firing an optional
   * callback after completion.
   */
  public Effects slideUp(Function... f) {
    return slideUp(Speed.DEFAULT, f);
  }

  /**
   * Hide all matched elements by adjusting their height and firing an optional
   * callback after completion.
   */
  public Effects slideUp(int millisecs, Function... f) {
    return animate("height: 'hide'", millisecs, f);
  }

  /**
   * Toggle displaying each of the set of matched elements by animating the
   * width, height, and opacity of the matched elements simultaneously. When
   * these properties reach 0 after a hiding animation, the display style
   * property is set to none to ensure that the element no longer affects the
   * layout of the page.
   */
  public Effects toggle(int millisecs, Function... f) {
    return animate("opacity: 'toggle', width : 'toggle', height : 'toggle'",
        millisecs, f);
  }
}
