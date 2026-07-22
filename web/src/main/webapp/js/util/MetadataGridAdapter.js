/**
 * MetadataGridAdapter
 * Translates explicit 12-column grid metadata into native responsive form grid classes.
 */
export class MetadataGridAdapter {
  /**
   * Adapts the grid configuration for a layout item.
   *
   * @param {Object} metadata The parent grid layout metadata.
   * @param {Object} fieldCoordinates The coordinates of the field in the grid.
   * @returns {Object} Translated layout properties containing the standard appearance attribute.
   */
  static translateLayout(metadata, fieldCoordinates) {
    if (!metadata || metadata.gridSystem !== '12-column' || !metadata.active) {
      return { appearance: this.getDefaultHeuristics(fieldCoordinates) };
    }

    if (!fieldCoordinates || typeof fieldCoordinates.span !== 'number') {
      return { appearance: this.getDefaultHeuristics(fieldCoordinates) };
    }

    const span = fieldCoordinates.span;
    // Map 12-column span to 4-column span (w1, w2, w3, w4)
    let scaledSpan = Math.round(span / 3);

    // Ensure bounds 1 to 4
    if (scaledSpan < 1) scaledSpan = 1;
    if (scaledSpan > 4) scaledSpan = 4;

    return {
      appearance: `w${scaledSpan}`,
    };
  }

  static getDefaultHeuristics(fieldCoordinates) {
    if (fieldCoordinates && fieldCoordinates.defaultAppearance) {
      return fieldCoordinates.defaultAppearance;
    }
    return '';
  }
}
