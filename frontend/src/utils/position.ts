export function positionForIndex(positions: number[], index: number): number {
  if (positions.length === 0) {
    return 1
  }
  if (index <= 0) {
    return positions[0] - 1
  }
  if (index >= positions.length) {
    return positions[positions.length - 1] + 1
  }
  return (positions[index - 1] + positions[index]) / 2
}
