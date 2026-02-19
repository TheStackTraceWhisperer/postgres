package application.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WidgetTest {

  @Test
  void testDefaultConstructor() {
    Widget widget = new Widget();
    assertNotNull(widget);
  }

  @Test
  void testConstructorWithTwoArguments() {
    String name = "Test Widget";
    OffsetDateTime createdAt = OffsetDateTime.now();

    Widget widget = new Widget(name, createdAt);

    assertEquals(name, widget.getName());
    assertEquals(createdAt, widget.getCreatedAt());
    assertNull(widget.getId());
    assertNull(widget.getQuantity());
    assertNull(widget.getPrice());
  }

  @Test
  void testConstructorWithFourArguments() {
    String name = "Test Widget";
    OffsetDateTime createdAt = OffsetDateTime.now();
    Integer quantity = 10;
    BigDecimal price = new BigDecimal("19.99");

    Widget widget = new Widget(name, createdAt, quantity, price);

    assertEquals(name, widget.getName());
    assertEquals(createdAt, widget.getCreatedAt());
    assertEquals(quantity, widget.getQuantity());
    assertEquals(price, widget.getPrice());
    assertNull(widget.getId());
  }

  @Test
  void testGetId() {
    Widget widget = new Widget();
    assertNull(widget.getId());
  }

  @Test
  void testGetName() {
    String name = "Alpha";
    Widget widget = new Widget(name, OffsetDateTime.now());
    assertEquals(name, widget.getName());
  }

  @Test
  void testGetCreatedAt() {
    OffsetDateTime createdAt = OffsetDateTime.now();
    Widget widget = new Widget("Test", createdAt);
    assertEquals(createdAt, widget.getCreatedAt());
  }

  @Test
  void testGetQuantity() {
    Integer quantity = 25;
    Widget widget = new Widget("Test", OffsetDateTime.now(), quantity, BigDecimal.TEN);
    assertEquals(quantity, widget.getQuantity());
  }

  @Test
  void testGetPrice() {
    BigDecimal price = new BigDecimal("29.99");
    Widget widget = new Widget("Test", OffsetDateTime.now(), 10, price);
    assertEquals(price, widget.getPrice());
  }

  @Test
  void testPriceWithDifferentScales() {
    BigDecimal price1 = new BigDecimal("19.99");
    BigDecimal price2 = new BigDecimal("19.9900");

    Widget widget1 = new Widget("Widget1", OffsetDateTime.now(), 10, price1);
    Widget widget2 = new Widget("Widget2", OffsetDateTime.now(), 10, price2);

    assertEquals(0, widget1.getPrice().compareTo(widget2.getPrice()));
  }

  @Test
  void testWidgetWithZeroQuantity() {
    Widget widget = new Widget("Zero Stock", OffsetDateTime.now(), 0, new BigDecimal("9.99"));
    assertEquals(0, widget.getQuantity());
  }

  @Test
  void testWidgetWithLargeQuantity() {
    Integer largeQuantity = 1000000;
    Widget widget = new Widget("Bulk Item", OffsetDateTime.now(), largeQuantity, BigDecimal.ONE);
    assertEquals(largeQuantity, widget.getQuantity());
  }

  @Test
  void testWidgetWithHighPrice() {
    BigDecimal highPrice = new BigDecimal("9999999.99");
    Widget widget = new Widget("Expensive", OffsetDateTime.now(), 1, highPrice);
    assertEquals(highPrice, widget.getPrice());
  }

  @Test
  void testWidgetWithLowPrice() {
    BigDecimal lowPrice = new BigDecimal("0.01");
    Widget widget = new Widget("Cheap", OffsetDateTime.now(), 1000, lowPrice);
    assertEquals(lowPrice, widget.getPrice());
  }
}

